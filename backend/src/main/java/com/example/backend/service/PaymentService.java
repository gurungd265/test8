package com.example.backend.service;

import com.example.backend.dto.PaymentResponseDto;
import com.example.backend.entity.*;
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
            Long orderId, String paymentMethodStr, BigDecimal amount, String transactionId) {

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

        // 4. String -> PaymentMethod enum 변환
        PaymentMethod paymentMethod;
        if (paymentMethodStr == null || paymentMethodStr.isEmpty()) {
            throw new IllegalArgumentException("결제 수단이 비어있습니다");
        }
        try {
            paymentMethod = PaymentMethod.valueOf(paymentMethodStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("無効な決済手段です: " + paymentMethodStr);
        }

        // 5. 결제 초기 상태로 생성
        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setAmount(amount);
        payment.setPaymentMethod(paymentMethod); // enum
        payment.setTransactionId(transactionId);
        payment.setStatus(PaymentStatus.INITIATED);

        // 6. 외부 결제 API 호출 (모킹으로 대체)
        try {
            boolean paymentSuccess = externalPaymentService.charge(paymentMethod.name(), amount, transactionId);

            if (paymentSuccess) {
                payment.setStatus(PaymentStatus.COMPLETED);
                order.setStatus(OrderStatus.COMPLETED);
            } else {
                payment.setStatus(PaymentStatus.FAILED);
                order.setStatus(OrderStatus.PAYMENT_FAILED);
            }

        } catch (Exception ex) {
            payment.setStatus(PaymentStatus.FAILED);
            order.setStatus(OrderStatus.PAYMENT_FAILED);
        }

        // 7. 저장
        paymentRepository.save(payment);
        orderRepository.save(order);

        // 8. DTO 반환 (enum -> String 변환)
        return PaymentResponseDto.builder()
                .id(payment.getId())
                .orderId(order.getId())
                .amount(payment.getAmount())
                .paymentMethod(payment.getPaymentMethod().name())
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
    @Transactional
    public PaymentResponseDto refundPayment(String transactionId, BigDecimal refundAmount) {
        // 1. transactionId로 결제 내역 조회, 없으면 예외 발생
        Payment payment = paymentRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new EntityNotFoundException("決済が見つかりません。"));

        // 2. 이미 전액 환불된 상태라면 추가 환불 불가, 예외 발생
        if (payment.getStatus() == PaymentStatus.REFUNDED) {
            throw new IllegalStateException("既に全額返金されています。");
        }

        // 3. 기존 환불 금액에 이번 환불 요청 금액을 더함 (기존 환불 없으면 이번 금액 그대로)
        BigDecimal newRefundAmount = payment.getRefundAmount() == null
                ? refundAmount
                : payment.getRefundAmount().add(refundAmount);

        // 4. 환불 총액이 결제 금액을 초과하면 예외 발생
        if (newRefundAmount.compareTo(payment.getAmount()) > 0) {
            throw new IllegalArgumentException("返金額が支払額を超えています。");
        }

        // 5. 외부 결제 시스템에 환불 요청 (모킹 처리: 무조건 true 반환)
        boolean refundSuccess = externalPaymentService.refund(transactionId, refundAmount);
        if (!refundSuccess) {
            throw new RuntimeException("返金処理に失敗しました。");
        }

        // 6. 결제 엔티티에 환불 금액 업데이트
        payment.setRefundAmount(newRefundAmount);

        // 7. 환불 금액과 결제 금액이 같으면 전액 환불, 아니면 부분 환불 처리
        Order order = payment.getOrder();

        if (newRefundAmount.compareTo(payment.getAmount()) == 0) {
            // 7-1. 전액 환불: 결제 상태와 주문 상태 모두 전액 환불 상태로 변경
            payment.setStatus(PaymentStatus.REFUNDED);
            if (order != null) {
                order.setStatus(OrderStatus.REFUNDED);
                orderRepository.save(order);
            }
        } else {
            // 7-2. 부분 환불: 결제 상태와 주문 상태를 부분 환불 상태로 변경
            payment.setStatus(PaymentStatus.PARTIALLY_REFUNDED);
            if (order != null) {
                order.setStatus(OrderStatus.PARTIALLY_REFUNDED);
                orderRepository.save(order);
            }
        }

        // 8. 결제 정보 업데이트 저장
        paymentRepository.save(payment);

        // 9. 결제 엔티티를 DTO로 변환하여 반환
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