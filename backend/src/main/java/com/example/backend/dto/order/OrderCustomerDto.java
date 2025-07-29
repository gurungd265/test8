package com.example.backend.dto.order;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderCustomerDto {

    @NotBlank(message = "姓は必須です。")
    @Pattern(regexp = "^[\\p{IsHan}]+$", message = "姓は漢字で入力してください。")
    private String lastName; // 성

    @NotBlank(message = "名は必須です。")
    @Pattern(regexp = "^[\\p{IsHan}]+$", message = "名は漢字で入力してください。")
    private String firstName; // 이름

    @NotBlank(message = "姓カナは必須です。")
    @Pattern(regexp = "^[\\u30A0-\\u30FF]+$", message = "姓カナはカタカナで入力してください。")
    private String lastNameKana;

    @NotBlank(message = "名カナは必須です。")
    @Pattern(regexp = "^[\\u30A0-\\u30FF]+$", message = "名カナはカタカナで入力してください。")
    private String firstNameKana;

    @NotBlank(message = "電話番号は必須です。")
    private String phone;

    @Email(message = "メールアドレスの形式が正しくありません。")
    @NotBlank(message = "メールアドレスは必須です。")
    private String email;
}