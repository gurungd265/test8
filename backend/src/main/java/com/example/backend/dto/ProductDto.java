package com.example.backend.dto;

import lombok.Data;

@Data
public class ProductDto {

    private String name;
    private String description;
    private double price;
    private int stockQuantity;
    private Long categoryId;
    private String categoryName;
    private String categorySlug;

}
