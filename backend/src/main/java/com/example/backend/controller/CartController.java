package com.example.backend.controller;

import com.example.backend.entity.Cart;
import com.example.backend.entity.CartItem;
import com.example.backend.service.CartService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    /**
     * ADMIN, USER & ANONYMOUS (non-login) accessible
     */

    // 카트 조회
    @GetMapping
    public ResponseEntity<Cart> getCart(
            @RequestParam String sessionId,
            Principal principal
    ) {
        // principal : JwtAuthenticationFilter가 JWT 검증 후 SecurityContext에 세팅한 인증 정보
        // principal이 null 아니면 로그인한 이메일 반환, null이면 비로그인 상태
        String userEmail = (principal != null) ? principal.getName() : null;
        Cart cart = cartService.getCartByUserEmailOrSessionId(userEmail, sessionId);
        return ResponseEntity.ok(cart);
    }

    // 카트에 상품 추가
    @PostMapping("/items")
    public ResponseEntity<CartItem> addProductToCart(
            @RequestParam(required = false) String sessionId,
            @RequestParam Long productId,
            @RequestParam int quantity,
            Principal principal
    ) {
        String userEmail = (principal != null) ? principal.getName() : null;
        CartItem cartItem = cartService.addProductToCart(userEmail, sessionId, productId, quantity);
        return ResponseEntity.ok(cartItem);
    }

    // 카트 상품 수량 변경
    @PutMapping("/items/{cartItemId}")
    public ResponseEntity<CartItem> updateCartItem(
            @PathVariable Long cartItemId,
            @RequestParam int quantity,
            @RequestParam(required = false) String sessionId,
            Principal principal
    ) {
        String userEmail = (principal != null) ? principal.getName() : null;
        CartItem updatedItem = cartService.updateCartItemQuantity(userEmail, sessionId, cartItemId, quantity);
        return ResponseEntity.ok(updatedItem);
    }

    // 카트 상품 삭제
    @DeleteMapping("/items/{cartItemId}")
    public ResponseEntity<Void> deleteCartItem(
            @PathVariable Long cartItemId,
            @RequestParam(required = false) String sessionId,
            Principal principal
    ) {
        String userEmail = (principal != null) ? principal.getName() : null;
        cartService.softDeleteCartItem(userEmail, sessionId, cartItemId);
        return ResponseEntity.noContent().build();
    }

    // 카트 전체 삭제
    @DeleteMapping
    public ResponseEntity<Void> clearCart(
            @RequestParam(required = false) String sessionId,
            Principal principal
    ) {
        String userEmail = (principal != null) ? principal.getName() : null;
        cartService.softClearCart(userEmail, sessionId);
        return ResponseEntity.noContent().build();
    }
}
