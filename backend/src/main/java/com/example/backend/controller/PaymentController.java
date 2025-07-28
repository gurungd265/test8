package com.example.backend.controller;

import com.example.backend.dto.PaymentRequestDto;
import com.example.backend.dto.PaymentResponseDto;
import com.example.backend.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    // 결제 요청
    @PostMapping
    public ResponseEntity<PaymentResponseDto> createPayment(
            @RequestBody PaymentRequestDto paymentRequest
    ) {
        // 결제 처리 서비스 호출
        PaymentResponseDto paymentResponseDto = paymentService.processPayment(
                paymentRequest.getOrderId(),
                paymentRequest.getPaymentMethod(),
                paymentRequest.getAmount(),
                paymentRequest.getTransactionId()
        );
        return new ResponseEntity<>(paymentResponseDto, HttpStatus.CREATED);
    }

    // 결제 취소
    @PostMapping("/cancel/{transactionId}")
    public ResponseEntity<PaymentResponseDto> cancelPayment(
            @PathVariable String transactionId
    ) {
        PaymentResponseDto canceledPayment = paymentService.cancelPayment(transactionId);
        return ResponseEntity.ok(canceledPayment);
    }

    // 환불 처리
    @PostMapping("/refund/{transactionId}")
    public ResponseEntity<PaymentResponseDto> refundPayment(
            @PathVariable String transactionId,
            @RequestParam BigDecimal refundAmount) {

        PaymentResponseDto refundedPayment = paymentService.refundPayment(transactionId, refundAmount);
        return ResponseEntity.ok(refundedPayment);
    }
}
