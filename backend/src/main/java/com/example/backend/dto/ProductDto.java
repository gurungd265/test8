package com.example.backend.dto;

import com.example.backend.entity.Product;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductDto {

    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private BigDecimal discountPrice;
    private int stockQuantity;

    private Long categoryId;
    private String categoryName;
    private String categorySlug;

    private List<ProductCharacteristicDto> characteristics;
    private List<ProductImageResponseDto> images;

    public static ProductDto fromEntity(Product product) {
        ProductDto dto = new ProductDto();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setDiscountPrice(product.getDiscountPrice());
        dto.setStockQuantity(product.getStockQuantity());

        if (product.getCategory() != null) {
            dto.setCategoryId(product.getCategory().getId());
            dto.setCategoryName(product.getCategory().getName());
            dto.setCategorySlug(product.getCategory().getSlug());
        }

        if (product.getCharacteristics() != null) {
            List<ProductCharacteristicDto> charDtos = product.getCharacteristics().stream()
                    .map(c -> new ProductCharacteristicDto(c.getCharacteristicName(), c.getCharacteristicValue()))
                    .toList();
            dto.setCharacteristics(charDtos);
        } else {
            dto.setCharacteristics(List.of());
        }

        // 이미지 리스트 매핑
        if (product.getProductImages() != null) {
            List<ProductImageResponseDto> imageDtos = product.getProductImages().stream()
                    .map(ProductImageResponseDto::fromEntity)
                    .toList();
            dto.setImages(imageDtos);
        } else {
            dto.setImages(List.of());
        }

        return dto;
    }

}

