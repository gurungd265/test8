package com.example.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddressDto {

    private Long id; // 수정 시 필요

    private String addressType; // "HOME", "WORK" 등 문자열로 받을 수 있음

    @NotBlank(message = "Streetは必須です。")
    private String street;

    @NotBlank(message = "Cityは必須です。")
    private String city;

    @NotBlank(message = "Stateは必須です。")
    private String state;

    @NotBlank(message = "Postal codeは必須です。")
    private String postalCode;

    @NotBlank(message = "Countryは必須です。")
    private String country;

    private Boolean isDefault;
}