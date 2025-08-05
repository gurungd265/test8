package com.example.backend.dto.payment;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeductReqDto {
    @NotBlank(message = "ユーザーIDは必須です。")
    private String userId;

    @Min(value = 1,message = "金額は1以上でなければなりません。")
    private int amount;
}
