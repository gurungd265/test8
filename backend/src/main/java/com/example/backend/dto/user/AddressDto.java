package com.example.backend.dto.user;

import com.example.backend.entity.user.AddressType;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AddressDto {

    private Long id;

    private AddressType addressType; // enum

    @NotBlank(message = "Postal codeは必須です。")
    private String postalCode;

    @NotBlank(message = "Streetは必須です。")
    private String state; // 都道府県

    @NotBlank(message = "Cityは必須です。")
    private String city; // 市区町村

    @NotBlank(message = "Stateは必須です。")
    private String street; // 番地、ビル、部屋番号

    @NotBlank(message = "Countryは必須です。")
    @Builder.Default
    private String country = "JAPAN";

    @Builder.Default
    private Boolean isDefault = false;
}