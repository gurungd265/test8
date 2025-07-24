package com.example.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductDto {

    private String name;
    private String description;
    private double price;
    private BigDecimal discountPrice;
    private int stockQuantity;
    private Long categoryId;
    private String categoryName;
    private String categorySlug;

    private List<ProductCharacteristicDto> characteristics;

}
