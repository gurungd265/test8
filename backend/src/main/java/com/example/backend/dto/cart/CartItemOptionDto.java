package com.example.backend.dto.cart;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CartItemOptionDto {
    private Long id;
    private Long productOptionId;
    private String optionName;
    private String optionValue;
}
