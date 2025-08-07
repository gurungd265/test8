package com.example.backend.dto.payment;

import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BalanceResponseDto {
    private BigDecimal pointBalance;
    private BigDecimal paypayBalance;
    private BigDecimal virtualCardBalance;
}
