package com.example.backend.dto.cart;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartDto {
    private Long id;
    private Long userId; // 회원
    private String sessionId; // 비회원
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private List<CartItemDto> items; // 담긴 상품 목록
    private int totalItemCount;
}