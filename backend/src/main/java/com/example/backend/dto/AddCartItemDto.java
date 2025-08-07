package com.example.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddCartItemDto {
    private Long productId;
    private int quantity;
    private List<CartItemOptionDto> options;
}
