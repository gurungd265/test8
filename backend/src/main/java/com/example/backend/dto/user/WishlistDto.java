package com.example.backend.dto.user;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WishlistDto {
    private Long id;

    @NotNull
    private Long productId;

    private String productName;
    private String productImageUrl;

    private BigDecimal price;
    private BigDecimal discountPrice;

    private LocalDateTime createdAt;
}
