package com.example.backend.controller;

import com.example.backend.dto.WishlistDto;
import com.example.backend.entity.user.User;
import com.example.backend.service.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/api/users/me/wishlists")
@RestController
@RequiredArgsConstructor
public class WishlistController {

    private final WishlistService wishlistService;

    // 현재 로그인한 사용자의 위시리스트 조회
    @GetMapping
    public ResponseEntity<List<WishlistDto>> getMyWishlist(
            @AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(wishlistService.getWishlistByUser(user.getId()));
    }

    // 현재 로그인한 사용자의 위시리스트에 상품 추가
    @PostMapping("/{productId}")
    public ResponseEntity<?> addProductToWishlist(
            @AuthenticationPrincipal User user,
            @PathVariable Long productId) {
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        try {
            wishlistService.addProductToWishlist(user.getId(), productId);
            return ResponseEntity.ok("商品が追加されました。");
        } catch (IllegalArgumentException e) {
            // 이미 위시리스트에 등록된 상품일 경우 에러 처리
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 현재 로그인한 사용자의 위시리스트에서 상품 소프트삭제
    @DeleteMapping("/products/{productId}")
    public ResponseEntity<?> removeProductFromWishlistByProductId(
            @AuthenticationPrincipal User user,
            @PathVariable Long productId) {
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        wishlistService.removeProductFromWishlistByProductId(productId, user.getId());
        return ResponseEntity.noContent().build();
    }
}