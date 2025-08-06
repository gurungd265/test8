package com.example.backend.controller;

import com.example.backend.dto.CartDto;
import com.example.backend.dto.CartItemDto;
import com.example.backend.dto.CartItemOptionDto;
import com.example.backend.dto.order.OrderResponseDto;
import com.example.backend.entity.CartItem;
import com.example.backend.service.CartService;
import com.example.backend.entity.user.User;

import com.example.backend.util.CookieUtil;
import com.example.backend.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;
import java.util.UUID;
import com.example.backend.repository.UserRepository;

import static com.example.backend.util.CookieUtil.CART_SESSION_ID;

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
    private final UserRepository userRepository;

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
            Principal principal,
            HttpServletRequest request,
            HttpServletResponse response,
            @RequestParam(value = "sessionId", required = false) String sessionIdFromRequestParam
    ) {
        String userEmail = (principal != null) ? principal.getName() : null;
        String sessionIdFromCookie = CookieUtil.getCookie(request, CART_SESSION_ID)
                .map(jakarta.servlet.http.Cookie::getValue)
                .orElse(null);

        // 쿼리 파라미터가 있으면 우선 사용, 없으면 쿠키에서 조회
        String sessionId = (sessionIdFromRequestParam != null) ? sessionIdFromRequestParam : sessionIdFromCookie;

        if (userEmail == null && sessionId == null) {
            String newSessionId = UUID.randomUUID().toString();
            CookieUtil.addCookie(response, CART_SESSION_ID, newSessionId, 60 * 60 * 24 * 7);
            CartDto cartDto = cartService.getCartByUserEmailOrSessionId(userEmail, newSessionId);
            return ResponseEntity.ok(cartDto);
        }

        CartDto cartDto = cartService.getCartByUserEmailOrSessionId(userEmail, sessionId);
        return ResponseEntity.ok(cartDto);
    }

    // 카트에 상품 추가
    @PostMapping("/items")
    public ResponseEntity<CartItemDto> addProductToCart(
            @RequestParam Long productId,
            @RequestParam int quantity,
            @RequestBody(required = false) List<CartItemOptionDto> optionDtos,
            Principal principal,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        String userEmail = null;
        if (principal != null) {
            userEmail = principal.getName();
        }
        String sessionIdFromCookie = CookieUtil.getCookie(request, CART_SESSION_ID)
                .map(jakarta.servlet.http.Cookie::getValue)
                .orElse(null);

        if (userEmail == null && sessionIdFromCookie == null) {
            sessionIdFromCookie = UUID.randomUUID().toString();
            CookieUtil.addCookie(response, CART_SESSION_ID, sessionIdFromCookie, 60 * 60 * 24 * 7);
        }

        String finalSessionId = sessionIdFromCookie;
        CartItem cartItem = cartService.addProductToCart(userEmail, finalSessionId, productId, quantity, optionDtos);
        CartItemDto dto = convertToDto(cartItem);
        return ResponseEntity.ok(dto);
    }

    // 카트 상품 수량 변경
    @PutMapping("/items/{cartItemId}")
    public ResponseEntity<CartItemDto> updateCartItem(
            @PathVariable Long cartItemId,
            @RequestParam int quantity,
            Principal principal,
            HttpServletRequest request
    ) {
        String userEmail = getUserEmail(principal);
        String sessionIdFromCookie = CookieUtil.getCookie(request, CART_SESSION_ID)
                .map(jakarta.servlet.http.Cookie::getValue)
                .orElse(null);
        CartItem updatedItem = cartService.updateCartItemQuantity(userEmail, sessionIdFromCookie, cartItemId, quantity);
        CartItemDto dto = convertToDto(updatedItem);
        return ResponseEntity.ok(dto);
    }

    // 카트 상품 삭제
    @DeleteMapping("/items/{cartItemId}")
    public ResponseEntity<Void> deleteCartItem(
            @PathVariable Long cartItemId,
            Principal principal,
            HttpServletRequest request
    ) {
        String userEmail = (principal != null) ? principal.getName() : null;
        String sessionIdFromCookie = CookieUtil.getCookie(request, CART_SESSION_ID)
                .map(jakarta.servlet.http.Cookie::getValue)
                .orElse(null);
        cartService.softDeleteCartItem(userEmail, sessionIdFromCookie, cartItemId);
        return ResponseEntity.noContent().build();
    }

    // 카트 전체 삭제
    @DeleteMapping
    public ResponseEntity<Void> clearCart(
            Principal principal,
            HttpServletRequest request
    ) {
        String userEmail = (principal != null) ? principal.getName() : null;
        String sessionIdFromCookie = CookieUtil.getCookie(request,CART_SESSION_ID)
                .map(jakarta.servlet.http.Cookie::getValue)
                .orElse(null);
        cartService.softClearCart(userEmail, sessionIdFromCookie);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/items/batch-delete")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteCartItemsBatch(
            @RequestBody List<Long> itemIds,
            Principal principal,
            HttpServletRequest request
    ) {
        String userEmail = (principal != null) ? principal.getName() : null;
        String sessionIdFromCookie = CookieUtil.getCookie(request, CART_SESSION_ID)
                .map(jakarta.servlet.http.Cookie::getValue)
                .orElse(null);

        cartService.softDeleteCartItemsBatch(userEmail, sessionIdFromCookie, itemIds);

        return ResponseEntity.noContent().build();
    }

    // ==================================================== Order ====================================================
    @GetMapping("/count")
    public ResponseEntity<Integer> getCartItemCount(
            Principal principal,
            HttpServletRequest request,
            @RequestParam(value = "sessionId", required = false) String sessionIdParam
    ) {
        try{
            if(principal!=null){
                String userEmail = principal.getName();
                User user = userRepository.findByEmail(userEmail)
                        .orElseThrow(() -> new RuntimeException("ユーザーが見つかりません。"));
                int count = cartService.getTotalCartItemCount(user);
                return ResponseEntity.ok(count);
            } else {
                if (sessionIdParam == null || sessionIdParam.isEmpty()) {
                    return ResponseEntity.ok(0);
                }
                int count = cartService.getTotalCartItemCount(null, sessionIdParam);
                return ResponseEntity.ok(count);
            }
        } catch ( Exception e ){
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(0);
        }
    }
    @PostMapping("/order")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<OrderResponseDto> createOrder(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).build(); // 비로그인 접근 차단
        }
        String userEmail = principal.getName();
        OrderResponseDto orderResponseDto = orderService.createOrderFromCart(userEmail);
        return ResponseEntity.ok(orderResponseDto);
    }

    // 편의 메소드 =====================================================================================================

    // CartItem -> DTO 변환
    private CartItemDto convertToDto(CartItem item) {
        return CartItemDto.builder()
                .id(item.getId())
                .productId(item.getProduct().getId())
                .productName(item.getProduct().getName())
                .productPrice(item.getProduct().getPrice())            //정가
                .priceAtAddition(item.getProduct().getDiscountPrice()) //할인가
                .productImageUrl(item.getProduct().getMainImageUrl())
                .quantity(item.getQuantity())
                .options(item.getOptions().stream()
                        .map(option -> CartItemOptionDto.builder()
                                .id(option.getId())
                                .productOptionId(option.getProductOption().getId())
                                .optionName(option.getProductOption().getOptionName())
                                .optionValue(option.getOptionValue())
                                .build())
                        .toList())
                .build();
    }

    // 사용자 이메일 추출
    private String getUserEmail(Principal principal) {
        return (principal != null) ? principal.getName() : null;
    }
}
