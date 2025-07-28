package com.example.backend.service;

import com.example.backend.dto.PaymentResponseDto;
import com.example.backend.entity.Order;
import com.example.backend.entity.OrderStatus;
import com.example.backend.entity.Payment;
import com.example.backend.entity.PaymentStatus;
import com.example.backend.repository.OrderRepository;
import com.example.backend.repository.PaymentRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final ExternalPaymentService externalPaymentService;

    @Transactional
    public PaymentResponseDto processPayment(
            Long orderId, String paymentMethod, BigDecimal amount, String transactionId) {

        // 1. 주문 조회
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("注文が見つかりません。"));

        // 2. 중복 결제 방지
        if (paymentRepository.findByTransactionId(transactionId).isPresent()) {
            throw new IllegalArgumentException("トランザクションIDが既に存在します。");
        }

        // 3. 결제 금액 확인
        if (order.getTotalAmount().compareTo(amount) != 0) {
            throw new IllegalArgumentException("決済金額が注文金額と一致しません。");
        }

        // 4. 결제 초기 상태로 생성
        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setAmount(amount);
        payment.setPaymentMethod(paymentMethod);
        payment.setTransactionId(transactionId);
        payment.setStatus(PaymentStatus.INITIATED);

        // 5. 외부 결제 API 호출 (모킹으로 대체)
        try {
            boolean paymentSuccess = externalPaymentService.charge(paymentMethod, amount, transactionId);

            if (paymentSuccess) {
                payment.setStatus(PaymentStatus.COMPLETED);
                order.setStatus(OrderStatus.COMPLETED);
            } else {
                payment.setStatus(PaymentStatus.FAILED);
                order.setStatus(OrderStatus.PAYMENT_FAILED);  // 실패 상태로 구분
            }

        } catch (Exception ex) {
            // 외부 결제 API 에러 발생 시 실패 처리
            payment.setStatus(PaymentStatus.FAILED);
            order.setStatus(OrderStatus.PAYMENT_FAILED);
        }

        // 6. 저장
        paymentRepository.save(payment);
        orderRepository.save(order);

        // 7. DTO 반환
        return PaymentResponseDto.builder()
                .id(payment.getId())
                .orderId(order.getId())
                .amount(payment.getAmount())
                .paymentMethod(payment.getPaymentMethod())
                .transactionId(payment.getTransactionId())
                .status(payment.getStatus().name())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .build();
    }

    // 결제 취소
    @Transactional
    public PaymentResponseDto cancelPayment(String transactionId) {
        Payment payment = paymentRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new EntityNotFoundException("決済が見つかりません。"));

        if (payment.getStatus() == PaymentStatus.CANCELED) {
            throw new IllegalStateException("取り消しされた決済です。");
        }

        // 외부 결제 시스템에 취소 요청
        boolean cancelSuccess = externalPaymentService.cancel(transactionId);
        if (!cancelSuccess) {
            throw new RuntimeException("決済取り消しに失敗しました。");
        }

        payment.setStatus(PaymentStatus.CANCELED); //결제 상태 변경
        paymentRepository.save(payment);

        Order order = payment.getOrder();
        if (order != null) {
            order.setStatus(OrderStatus.CANCELLED); //주문 상태 변경
            orderRepository.save(order);
        }

        return PaymentResponseDto.fromEntity(payment);
    }

    // 환불 처리
    public PaymentResponseDto refundPayment(String transactionId, BigDecimal refundAmount) {
        Payment payment = paymentRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new EntityNotFoundException("決済が見つかりません。"));

        if (payment.getStatus() == PaymentStatus.REFUNDED) {
            throw new IllegalStateException("既に全額返金されています。");
        }

        BigDecimal newRefundedAmount = payment.getRefundAmount() == null
                ? refundAmount
                : payment.getRefundAmount().add(refundAmount);

        if (newRefundedAmount.compareTo(payment.getAmount()) > 0) {
            throw new IllegalArgumentException("返金額が支払額を超えています。");
        }

        // 외부 환불 요청 (모킹)
        boolean refundSuccess = externalPaymentService.refund(transactionId, refundAmount);
        if (!refundSuccess) {
            throw new RuntimeException("返金処理に失敗しました。");
        }

        payment.setRefundAmount(newRefundedAmount);

        if (newRefundedAmount.compareTo(payment.getAmount()) == 0) {
            payment.setStatus(PaymentStatus.REFUNDED); // 전액 환불
        } else {
            payment.setStatus(PaymentStatus.PARTIALLY_REFUNDED); // 부분 환불
        }

        paymentRepository.save(payment);

        // 필요 시 주문 상태도 업데이트

        return PaymentResponseDto.fromEntity(payment);
    }

    // 특정 주문별 결제내역 조회
    public List<PaymentResponseDto> getPaymentsByOrderId(Long orderId, String userEmail) {
        // 주문이 해당 유저의 주문인지 확인
        Order order = orderRepository.findByIdAndUserEmail(orderId, userEmail)
                .orElseThrow(() -> new EntityNotFoundException("Order not found or access denied"));

        List<Payment> payments = paymentRepository.findByOrder(order);

        return payments.stream()
                .map(PaymentResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    // 결제상태별 결제내역 목록 조회
    @Transactional(readOnly = true)
    public List<PaymentResponseDto> getPaymentsByStatus(PaymentStatus status) {
        List<Payment> payments = paymentRepository.findByStatus(status);
        return payments.stream()
                .map(PaymentResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

}