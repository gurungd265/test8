package com.example.backend.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class BalanceResponseDto {
    private int pointBalance;
    private int paypayBalance;
}
