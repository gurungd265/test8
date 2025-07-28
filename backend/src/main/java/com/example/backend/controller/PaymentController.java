package com.example.backend.controller;

import com.example.backend.dto.PaymentRequestDto;
import com.example.backend.dto.PaymentResponseDto;
import com.example.backend.entity.PaymentStatus;
import com.example.backend.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j //log
public class PaymentController {

    private final PaymentService paymentService;

    // 결제 요청
    @PostMapping
    public ResponseEntity<PaymentResponseDto> createPayment(
            @RequestBody PaymentRequestDto paymentRequest
    ) {
        // 결제 처리 서비스 호출
        log.info("Payment request received: orderId={}, paymentMethod={}, amount={}, transactionId={}",
                paymentRequest.getOrderId(), paymentRequest.getPaymentMethod(),
                paymentRequest.getAmount(), paymentRequest.getTransactionId());
        PaymentResponseDto paymentResponseDto = paymentService.processPayment(
                paymentRequest.getOrderId(),
                paymentRequest.getPaymentMethod(),
                paymentRequest.getAmount(),
                paymentRequest.getTransactionId()
        );
        log.info("Payment processed successfully: {}", paymentResponseDto);
        return new ResponseEntity<>(paymentResponseDto, HttpStatus.CREATED);
    }

    // 결제 취소
    @PostMapping("/cancel/{transactionId}")
    public ResponseEntity<PaymentResponseDto> cancelPayment(
            @PathVariable String transactionId
    ) {
        log.info("Payment cancellation request received: transactionId={}", transactionId);
        PaymentResponseDto canceledPayment = paymentService.cancelPayment(transactionId);
        log.info("Payment cancelled successfully: {}", canceledPayment);
        return ResponseEntity.ok(canceledPayment);
    }

    // 환불 처리
    @PostMapping("/refund/{transactionId}")
    public ResponseEntity<PaymentResponseDto> refundPayment(
            @PathVariable String transactionId,
            @RequestParam BigDecimal refundAmount) {
        log.info("Refund request received: transactionId={}, refundAmount={}", transactionId, refundAmount);
        PaymentResponseDto refundedPayment = paymentService.refundPayment(transactionId, refundAmount);
        log.info("Refund processed successfully: {}", refundedPayment);
        return ResponseEntity.ok(refundedPayment);
    }

    // 결제 상태별 결제 내역 목록 조회
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PaymentResponseDto>> getPaymentsByStatus(@PathVariable PaymentStatus status) {
        List<PaymentResponseDto> payments = paymentService.getPaymentsByStatus(status);
        return ResponseEntity.ok(payments);
    }

}
