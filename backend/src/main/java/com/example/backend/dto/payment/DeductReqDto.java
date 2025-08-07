package com.example.backend.dto.payment;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class DeductReqDto {
    @NotBlank(message = "ユーザーIDは必須です。")
    private String userId;

    private BigDecimal amount;
}
