package com.example.backend.dto.user;

import com.example.backend.entity.payment.PaymentMethod;
import com.example.backend.entity.user.UserBalance;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserBalanceResponseDto {
    private Long id;
    private String userId;
    private PaymentMethod paymentMethod;
    private int balance;
    private LocalDateTime updatedAt;

    public UserBalanceResponseDto(UserBalance userBalance){
        this.id = userBalance.getId();
        this.userId = userBalance.getUserId();
        this.paymentMethod = userBalance.getPaymentMethod();
        this.balance = userBalance.getBalance();
        this.updatedAt = userBalance.getUpdatedAt();
    }
}
