package com.example.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItemDto {
    private Long id;

    // from Product Entity
    private Long productId;
    private String productName;
    private BigDecimal productPrice;        // 정가
    private BigDecimal priceAtAddition;     // 할인가 (product.discountPrice)
    private String productImageUrl;

    private int quantity;
}
