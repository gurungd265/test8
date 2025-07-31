package com.example.backend.service;

import com.example.backend.dto.CartDto;
import com.example.backend.dto.CartItemDto;
import com.example.backend.entity.Cart;
import com.example.backend.entity.CartItem;
import com.example.backend.entity.Product;
import com.example.backend.entity.user.User;
import com.example.backend.repository.CartRepository;
import com.example.backend.repository.CartItemRepository;
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
    private final CartItemRepository cartItemRepository;

    /**
     * 카트에 상품 추가
     * - 로그인 유저(userEmail) 또는 비회원(sessionId) 기준 카트 조회 및 생성
     * - 재고 수량 체크 후, 기존 아이템 수량 업데이트 또는 신규 아이템 추가
     */
     @Transactional
    public CartItem addProductToCart(String userEmail, String sessionId, Long productId, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("追加する数量は1以上である必要があります。");
        }

        // 카트 조회 또는 생성
        Cart cart = getOrCreateCart(userEmail, sessionId);

        // 상품 조회
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("商品が見つかりません。"));

        // 카트에 이미 존재하는 아이템 확인
        Optional<CartItem> existingItemOpt = cart.getCartItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst();

        int newQuantity = quantity;
        if (existingItemOpt.isPresent()) {
            // 기존 수량에 추가
            newQuantity += existingItemOpt.get().getQuantity();
        }

        // 재고 확인
        if (product.getStockQuantity() == null || product.getStockQuantity() < newQuantity) {
            throw new IllegalStateException("在庫切れです");
        }

        CartItem cartItem;
         if (existingItemOpt.isPresent()) {
             cartItem = existingItemOpt.get();
             cartItem.setQuantity(newQuantity);
         } else {
             cartItem = new CartItem();
             cartItem.setCart(cart);
             cartItem.setProduct(product);
             cartItem.setQuantity(quantity);
             cartItem.setPriceAtAddition(product.getDiscountPrice()); // ✔ 할인 가격 저장
             cart.addCartItem(cartItem);
         }

        // 변경사항 저장
        cartItemRepository.save(cartItem);

        return cartItem;
    }

    /**
     비회원에서 회원 로그인 시 카트 병합
     * - 로그인 후 세션 기반 카트를 유저 카트에 병합하는 기능
     * - 중복 아이템은 수량 합산, 세션 카트는 소프트 삭제 처리
     */
    @Transactional
    public void mergeCarts(String userEmail, String sessionId) {
        if (userEmail == null || sessionId == null) {
            throw new IllegalArgumentException("ユーザーのメールアドレスとセッションIDは必須です");
        }


        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("ユーザーが見つかりません。"));

        Optional<Cart> userCartOpt = cartRepository.findByUserAndDeletedAtIsNull(user);
        Cart userCart;

        if (userCartOpt.isPresent()) {
            userCart = userCartOpt.get();
        } else {
            userCart = new Cart();
            userCart.setUser(user);
            cartRepository.save(userCart); // 새 카트 미리 저장하여 ID 할당
        }

        Cart sessionCart = cartRepository.findBySessionIdAndDeletedAtIsNull(sessionId)
                .orElse(null);

        if (sessionCart == null || sessionCart.getCartItems().isEmpty()) { // 세션 카트가 비어있어도 종료
            // 세션 카트가 비어있어도 삭제 처리는 해주는게 좋습니다.
            if (sessionCart != null) {
                sessionCart.setDeletedAt(LocalDateTime.now());
                cartRepository.save(sessionCart);
            }
            return;
        }

        // 세션 카트 아이템을 유저 카트로 병합
        for (CartItem sessionItem : sessionCart.getCartItems()) {
            if (sessionItem.getDeletedAt() != null) continue;
            Optional<CartItem> existingItemOpt = cartItemRepository.findByCartAndProductIdAndDeletedAtIsNull(userCart, sessionItem.getProduct().getId());
            if (existingItemOpt.isPresent()) { // 중복 상품 수량 합산
                CartItem existingItem = existingItemOpt.get();
                existingItem.setQuantity(existingItem.getQuantity() + sessionItem.getQuantity());
                // ⭐ 재고 확인 추가
                if (existingItem.getProduct().getStockQuantity() == null ||
                        existingItem.getProduct().getStockQuantity() < (existingItem.getQuantity())) {
                    // 재고 부족 시 예외 발생 또는 가능한 만큼만 추가하는 로직 구현
                    throw new IllegalStateException("商品'" + existingItem.getProduct().getName() + "'の在庫が不足しているため、カートの結合に失敗しました。");
                }
                cartItemRepository.save(existingItem);
            } else { // 새로운 상품 추가 (새로운 CartItem 인스턴스 생성)
                CartItem newCartItem = new CartItem();
                newCartItem.setCart(userCart);
                newCartItem.setProduct(sessionItem.getProduct());
                newCartItem.setQuantity(sessionItem.getQuantity());
                newCartItem.setPriceAtAddition(sessionItem.getPriceAtAddition());
                newCartItem.setAddedAt(LocalDateTime.now());
                userCart.addCartItem(newCartItem);
                cartItemRepository.save(newCartItem); // 새 아이템의 카트를 유저 카트로 설정
            }
        }

        // 세션 카트 소프트 삭제 (이전 아이템들도 이제 유저 카트에 복사되었으므로)
        sessionCart.setDeletedAt(LocalDateTime.now());
//        sessionCart.getCartItems().clear();

        // 변경된 카트 저장
        cartRepository.save(userCart); // 사용자 카트 저장 (CartItem 변경도 cascade로 반영될 수 있음)
        cartRepository.save(sessionCart); // 세션 카트의 deletedAt 업데이트 저장
    }

    @Transactional
    public void softDeleteCartItemsByProductId(Long productId) {
        cartItemRepository.softDeleteByProductId(productId);
    }

    /**
     카트 아이템 목록 조회 (유저 이메일 또는 세션 ID 기반)
     * - 카트가 없으면 생성
     * - 카트와 카트 아이템 리스트를 DTO로 변환 후 반환
     */
    @Transactional(readOnly = true)
    public CartDto getCartByUserEmailOrSessionId(String userEmail, String sessionId) {
        Cart cart = getOrCreateCart(userEmail, sessionId);
        return convertToDto(cart);
    }

    /**
     카트 아이템 수량 변경
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

        cartItemRepository.save(cartItem);
        return cartItem;
    }

    /**
     카트 아이템 소프트 삭제
     */
    @Transactional
    public void softDeleteCartItem(String userEmail, String sessionId, Long cartItemId) {
        Cart cart = getOrCreateCart(userEmail, sessionId);

        CartItem cartItem = cart.getCartItems().stream()
                .filter(item -> item.getId().equals(cartItemId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("カートアイテムが見つかりません。"));
        cartItem.setDeletedAt(LocalDateTime.now());
        cartItemRepository.save(cartItem); // CartItem 직접 저장
    }

    /**
     카트 전체 소프트 삭제
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
            Optional<Cart> existingUserCart = cartRepository.findByUserAndDeletedAtIsNull(user);
            if (existingUserCart.isPresent()) {
                return existingUserCart.get();
            } else {
                Cart newCart = new Cart();
                newCart.setUser(user);
                return cartRepository.save(newCart);
            }
        } else if (sessionId != null) {
            return cartRepository.findBySessionIdAndDeletedAtIsNull(sessionId)
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
                        item.getProduct().getPrice(),           // 정가
                        item.getPriceAtAddition(),              // 할인가 (카트 담긴 시점)
                        item.getProduct().getMainImageUrl(),
                        item.getQuantity()
                ))
                .toList();

        return new CartDto(
                cart.getId(),
                cart.getUser() != null ? cart.getUser().getId() : null,
                cart.getSessionId(),
                cart.getCreatedAt(),
                cart.getUpdatedAt(),
                items,
                items.size()
        );
    }

    //카트 카운트 계산
    @Transactional(readOnly = true)
    public int getTotalCartItemCount(String userEmail, String sessionId) {
        Cart cart = getOrCreateCart(userEmail,sessionId);
        return (int) cart.getCartItems().stream().filter(item -> item.getDeletedAt() == null).count();
    }

    // ⭐ 새로 추가: 카트 카운트 계산 (User 객체를 직접 받는 오버로드 메서드)
    @Transactional(readOnly = true)
    public int getTotalCartItemCount(User user) {
        Optional<Cart> cartOpt = cartRepository.findByUserAndDeletedAtIsNull(user); // User 객체로 카트 조회
        if (cartOpt.isPresent()) {
            return (int) cartOpt.get().getCartItems().stream().filter(item -> item.getDeletedAt() == null).count();
        }
        return 0;
    }
}