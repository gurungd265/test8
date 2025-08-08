package com.example.backend.dto.product;

import com.example.backend.entity.product.ProductImage;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductImageResponseDto {
    private Long id;
    private String imageUrl;

    public static ProductImageResponseDto fromEntity(ProductImage image) {
        return ProductImageResponseDto.builder()
                .id(image.getId())
                .imageUrl(image.getImageUrl())
                .build();
    }
}
