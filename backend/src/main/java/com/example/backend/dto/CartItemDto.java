package com.example.backend.dto;

import com.example.backend.entity.CartItemOption;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

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

    private List<CartItemOptionDto> options;
}
