package com.example.backend.controller;

import com.example.backend.dto.CartDto;
import com.example.backend.dto.CartItemDto;
import com.example.backend.dto.OrderDto;
import com.example.backend.entity.Cart;
import com.example.backend.entity.CartItem;
import com.example.backend.service.CartService;

import com.example.backend.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    /**
     * ADMIN, USER & ANONYMOUS (non-login) accessible
     *
     * 카트 조회	                GET	    /api/cart
     * 카트에 상품 추가	        POST	/api/cart/items
     * 카트 상품 수정	            PUT	    /api/cart/items/{id}
     * 카트 상품 삭제	            DELETE	/api/cart/items/{id}
     * 카트 전체 삭제	            DELETE	/api/cart
     * 비로그인/로그인 카트 병합	POST	/api/cart/merge
     * 주문 생성	                POST	/api/cart/order
     */

    private final CartService cartService;
    private final OrderService orderService;

    //유저 카트 병합
    @PostMapping("/merge")
    public ResponseEntity<Void> mergeCarts(
            Principal principal,
            @RequestParam(required = true) String sessionId
    ){
        String userEmail = principal.getName();
        cartService.mergeCarts(userEmail,sessionId);
        return ResponseEntity.ok().build();
    }

    // 카트 조회
    @GetMapping
    public ResponseEntity<CartDto> getCart(
            @RequestParam(required = false) String sessionId,
            Principal principal
    ) {
        // principal : JwtAuthenticationFilter가 JWT 검증 후 SecurityContext에 세팅한 인증 정보
        // principal이 null 아니면 로그인한 이메일 반환, null이면 비로그인 상태
        String userEmail = (principal != null) ? principal.getName() : null;
        CartDto cartDto = cartService.getCartByUserEmailOrSessionId(userEmail, sessionId);
        return ResponseEntity.ok(cartDto);
    }

    // 카트에 상품 추가
    @PostMapping("/items")
    public ResponseEntity<CartItemDto> addProductToCart(
            @RequestParam(required = false) String sessionId,
            @RequestParam Long productId,
            @RequestParam int quantity,
            Principal principal
    ) {
        String userEmail = getUserEmail(principal);
        CartItem cartItem = cartService.addProductToCart(userEmail, sessionId, productId, quantity);
        CartItemDto dto = convertToDto(cartItem);
        return ResponseEntity.ok(dto);
    }

    // 카트 상품 수량 변경
    @PutMapping("/items/{cartItemId}")
    public ResponseEntity<CartItemDto> updateCartItem(
            @PathVariable Long cartItemId,
            @RequestParam int quantity,
            @RequestParam(required = false) String sessionId,
            Principal principal
    ) {
        String userEmail = getUserEmail(principal);
        CartItem updatedItem = cartService.updateCartItemQuantity(userEmail, sessionId, cartItemId, quantity);
        CartItemDto dto = convertToDto(updatedItem);
        return ResponseEntity.ok(dto);
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

    // ==================================================== Order ====================================================
    @PostMapping("/order")
    public ResponseEntity<OrderDto> createOrder(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).build(); // 비로그인 접근 차단
        }
        String userEmail = principal.getName();
        OrderDto orderDto = orderService.createOrderFromCart(userEmail);
        return ResponseEntity.ok(orderDto);
    }


    // 편의 메소드 =====================================================================================================

    // CartItem -> DTO 변환
    private CartItemDto convertToDto(CartItem item) {
        return new CartItemDto(
                item.getId(),
                item.getProduct().getId(),
                item.getProduct().getName(),
                item.getQuantity(),
                item.getPriceAtAddition()
        );
    }
    
    // 사용자 이메일 추출
    private String getUserEmail(Principal principal) {
        return (principal != null) ? principal.getName() : null;
    }
}
