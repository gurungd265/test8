package com.example.backend.dto.payment;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class CardRegistrationRequestDto {
    @NotBlank
    private String userId;

    @NotBlank
    private String cardCompanyName;

    @NotBlank
    @Pattern(regexp = "\\d{16}", message = "カード番号は16桁の数字でなければなりません。")
    private String cardNumber;

    @NotBlank
    private String cardHolderName;

    @NotBlank
    @Pattern(regexp = "\\d{4}", message = "満了日はMMYY形式の4桁の数字でなければなりません。")
    private String expiryDate;

    @NotBlank
    @Pattern(regexp = "\\d{3}", message = "CVVは3桁の数字でなければなりません。")
    private String cvv; // just 1time
}
