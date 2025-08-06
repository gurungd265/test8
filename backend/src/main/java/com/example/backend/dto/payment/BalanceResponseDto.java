package com.example.backend.dto.payment;

import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BalanceResponseDto {
    private BigDecimal pointBalance;
    private BigDecimal paypayBalance;
    private int virtualCardBalance;

//    public BalanceResponseDto(BigDecimal pointBalance, BigDecimal paypayBalance, int virtualCardBalance) {
//        this.pointBalance = pointBalance;
//        this.paypayBalance = paypayBalance;
//        this.virtualCardBalance = virtualCardBalance;
//    }

    public BalanceResponseDto(BigDecimal pointBalance, BigDecimal paypayBalance) {
        this.pointBalance = pointBalance;
        this.paypayBalance = paypayBalance;
        this.virtualCardBalance = 0; // 카드 잔액은 0으로 기본값 설정 (이 생성자에서는 사용되지 않으므로)
    }
}
