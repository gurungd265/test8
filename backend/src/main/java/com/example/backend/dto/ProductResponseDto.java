package com.example.backend.dto;

import com.example.backend.entity.Product;
import com.example.backend.entity.ProductImage;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import java.time.LocalDateTime;

@Data
@Builder
public class ProductResponseDto {
    private long id;
    private String name;
    private String description;
    private double price;
    private BigDecimal discountPrice;
    private int stockQuantity;
    private Long categoryId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String categoryName;
    private String categorySlug;

    private List<ProductImageResponseDto> productImages;

    public static ProductResponseDto fromEntity(Product product){
        List<ProductImageResponseDto> imageDtos = product.getProductImages()!= null ?
                product.getProductImages().stream()
                        .map(ProductImageResponseDto::fromEntity)
                        .collect(Collectors.toList()):
                List.of();

        return ProductResponseDto.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .discountPrice(product.getDiscountPrice())
                .stockQuantity(product.getStockQuantity())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .productImages(imageDtos)
                .build();
    }
}
