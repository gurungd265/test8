package com.example.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaymentRequestDto {    // 클라이언트 -> 서버로 결제 요청할 때 보내는 데이터용
    private Long orderId;
    private String paymentMethod;
    private BigDecimal amount;
    private BigDecimal refundedAmount;
    private String transactionId;
}
