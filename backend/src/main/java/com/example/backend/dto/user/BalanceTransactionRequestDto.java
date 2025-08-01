package com.example.backend.dto.user;

import com.example.backend.entity.payment.PaymentMethod;
import lombok.Data;

@Data
public class BalanceTransactionRequestDto {
    private String userId;
    private PaymentMethod paymentMethod;
    private int amount;
}
