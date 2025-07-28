package com.example.backend.dto;

import com.example.backend.entity.payment.Payment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaymentResponseDto {       // 서버 -> 클라이언트로 데이터 반환하는 결과용

    private Long id;
    private Long orderId;
    private BigDecimal amount;
    private BigDecimal refundAmount;
    private String paymentMethod;       // enum -> String
    private String transactionId;
    private String status;              // enum -> String
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Payment Entity -> Dto 변환 메서드
    public static PaymentResponseDto fromEntity(Payment payment) {
        return PaymentResponseDto.builder()
                .id(payment.getId())
                .orderId(payment.getOrder().getId())
                .amount(payment.getAmount())
                .refundAmount(payment.getRefundAmount())
                .paymentMethod(payment.getPaymentMethod().name())
                .transactionId(payment.getTransactionId())
                .status(payment.getStatus().name())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .build();
    }

}
