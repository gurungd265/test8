package com.example.backend.service;

import com.example.backend.dto.CartDto;
import com.example.backend.dto.CartItemDto;
import com.example.backend.entity.Cart;
import com.example.backend.entity.CartItem;
import com.example.backend.entity.Product;
import com.example.backend.entity.user.User;
import com.example.backend.repository.CartRepository;
import com.example.backend.repository.ProductRepository;
import com.example.backend.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    /**
     * 카트에 상품 추가
     *
     * @param userEmail 로그인된 유저 이메일 (비회원은 null)
     * @param sessionId 비회원 세션ID (로그인 유저는 null)
     * @param productId 추가할 상품 ID
     * @param quantity 수량
     * @return CartItem반영
     */
    @Transactional
    public CartItem addProductToCart(String userEmail, String sessionId, Long productId, int quantity) {
        // 1. 유저 또는 세션 기반 카트 조회/생성
        Cart cart = getOrCreateCart(userEmail, sessionId);

        // 2. 상품 조회
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("商品が見つかりません。"));

        // 3. 이미 카트에 상품이 있으면 수량 업데이트, 없으면 새로 추가
        Optional<CartItem> existingItemOpt = cart.getCartItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst();

        CartItem cartItem;
        if (existingItemOpt.isPresent()) {
            cartItem = existingItemOpt.get();
            cartItem.setQuantity(cartItem.getQuantity() + quantity);
        } else {
            cartItem = new CartItem();
            cartItem.setCart(cart);
            cartItem.setProduct(product);
            cartItem.setQuantity(quantity);
            cartItem.setPriceAtAddition(BigDecimal.valueOf(product.getPrice())); // 가격 저장
            cart.getCartItems().add(cartItem);
        }

        // 저장
        cartRepository.save(cart);

        return cartItem;
    }

    /**
     비회원에서 회원 로그인 시 장바구니 병합
     */
    @Transactional
    public void mergeCarts(String userEmail, String sessionId) {
        if (userEmail == null || sessionId == null) {
            throw new IllegalArgumentException("ユーザーのメールアドレスとセッションIDは必須です");
        }

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("ユーザーが見つかりません。"));

        Cart userCart = cartRepository.findByUser(user)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUser(user);
                    return cartRepository.save(newCart);
                });

        Cart sessionCart = cartRepository.findBySessionId(sessionId)
                .orElse(null);

        if (sessionCart == null) {
            return; // 병합할 카트가 없으면 종료
        }

        for (CartItem sessionItem : sessionCart.getCartItems()) {
            if (sessionItem.getDeletedAt() != null) continue; // 삭제된 아이템 무시

            Optional<CartItem> existingItemOpt = userCart.getCartItems().stream()
                    .filter(item -> item.getProduct().getId().equals(sessionItem.getProduct().getId()) && item.getDeletedAt() == null)
                    .findFirst();

            if (existingItemOpt.isPresent()) {
                CartItem existingItem = existingItemOpt.get();
                existingItem.setQuantity(existingItem.getQuantity() + sessionItem.getQuantity());
            } else {
                sessionItem.setCart(userCart);
                userCart.getCartItems().add(sessionItem);
            }
        }
        // 세션 카트 소프트 삭제
        sessionCart.setDeletedAt(LocalDateTime.now());

        cartRepository.save(userCart);
        cartRepository.save(sessionCart);
    }

    /**
     카트 상품 목록 조회
     */
    @Transactional(readOnly = true)
    public CartDto getCartByUserEmailOrSessionId(String userEmail, String sessionId) {
        Cart cart = getOrCreateCart(userEmail, sessionId);
        return convertToDto(cart);
    }

    /**
     카트 상품 수량 변경
     */
    @Transactional
    public CartItem updateCartItemQuantity(String userEmail, String sessionId, Long cartItemId, int newQuantity) {
        Cart cart = getOrCreateCart(userEmail, sessionId);

        CartItem cartItem = cart.getCartItems().stream()
                .filter(item -> item.getId().equals(cartItemId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("カートアイテムが見つかりません。"));

        if (newQuantity <= 0) {
            // 수량 0 이하면 소프트 삭제 처리
            cartItem.setDeletedAt(LocalDateTime.now());
        } else {
            cartItem.setQuantity(newQuantity);
        }

        cartRepository.save(cart);
        return cartItem;
    }

    /**
     카트 상품 삭제
     */
    @Transactional
    public void softDeleteCartItem(String userEmail, String sessionId, Long cartItemId) {
        Cart cart = getOrCreateCart(userEmail, sessionId);

        CartItem cartItem = cart.getCartItems().stream()
                .filter(item -> item.getId().equals(cartItemId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("カートアイテムが見つかりません。"));
        // 소프트 삭제 처리
        cartItem.setDeletedAt(LocalDateTime.now());
        cartRepository.save(cart);
    }

    /**
     카트 전체 삭제
     */
    @Transactional
    public void softClearCart(String userEmail, String sessionId) {
        Cart cart = getOrCreateCart(userEmail, sessionId);

        LocalDateTime now = LocalDateTime.now();
        for (CartItem item : cart.getCartItems()) {
            item.setDeletedAt(now);
        }
        cart.setDeletedAt(now);

        cartRepository.save(cart);
    }

    /**
     편의 메소드 ====================================================================================================
     */

    // 유저 이메일 또는 세션 아이디 기반 카트 가져오기 or 생성
    private Cart getOrCreateCart(String userEmail, String sessionId) {
        if (userEmail != null) {
            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new UsernameNotFoundException("ユーザーが見つかりません。"));
            return cartRepository.findByUser(user)
                    .orElseGet(() -> {
                        Cart newCart = new Cart();
                        newCart.setUser(user);
                        return cartRepository.save(newCart);
                    });
        } else if (sessionId != null) {
            return cartRepository.findBySessionId(sessionId)
                    .orElseGet(() -> {
                        Cart newCart = new Cart();
                        newCart.setSessionId(sessionId);
                        return cartRepository.save(newCart);
                    });
        } else {
            throw new IllegalArgumentException("ユーザーのメールアドレスかセッションIDのいずれかを指定してください");
        }
    }

    // Cart -> CartDto 변환
    private CartDto convertToDto(Cart cart) {
        List<CartItemDto> items = cart.getCartItems().stream()
                .filter(item -> item.getDeletedAt() == null)
                .map(item -> new CartItemDto(
                        item.getId(),
                        item.getProduct().getId(),
                        item.getProduct().getName(),
                        item.getQuantity(),
                        item.getPriceAtAddition()
                ))
                .toList();

        return new CartDto(
                cart.getId(),
                cart.getUser() != null ? cart.getUser().getId() : null,
                cart.getSessionId(),
                cart.getCreatedAt(),
                cart.getUpdatedAt(),
                items
        );
    }
}