package com.example.backend.service;

import com.example.backend.dto.WishlistDto;
import com.example.backend.entity.Product;
import com.example.backend.entity.Wishlist;
import com.example.backend.entity.user.User;
import com.example.backend.repository.ProductImageRepository;
import com.example.backend.repository.ProductRepository;
import com.example.backend.repository.UserRepository;
import com.example.backend.repository.WishlistRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.backend.dto.WishlistDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;

    // 위시리스트 목록 조회 (유저별)
    @Transactional(readOnly = true)
    public List<WishlistDto> getWishlistByUser(Long userId) {
        List<Wishlist> wishlists = wishlistRepository.findByUserIdAndDeletedAtIsNull(userId);
        return wishlists.stream().map(this::toDto).toList();
    }

    // 위시리스트에 상품 추가
    public void addProductToWishlist(Long userId, Long productId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));

        // 이미 등록된 상품이면 예외 혹은 무시 처리 가능
        boolean exists = wishlistRepository.existsByUserAndProduct(user, product);
        if (exists) {
            throw new IllegalArgumentException("Product already in wishlist");
        }

        Wishlist wishlist = new Wishlist();
        wishlist.setUser(user);
        wishlist.setProduct(product);
        wishlistRepository.save(wishlist);
    }

    // 위시리스트에서 상품 소프트 삭제
    @Transactional
    public void removeProductFromWishlistByProductId(Long productId, Long userId) {
        Optional<Wishlist> wishlistOptional = wishlistRepository.findByUserIdAndProductIdAndDeletedAtIsNull(userId, productId);

        if (wishlistOptional.isPresent()) {
            Wishlist wishlist = wishlistOptional.get();
            wishlist.setDeletedAt(LocalDateTime.now());
            wishlistRepository.save(wishlist);
        } else {
            throw new EntityNotFoundException("指定された商品がウィッシュリストに見つからないか、既に削除されています。");
        }
    }

    private WishlistDto toDto(Wishlist wishlist) {
        WishlistDto dto = new WishlistDto();
        dto.setId(wishlist.getId());
        dto.setProductId(wishlist.getProduct().getId());
        dto.setProductName(wishlist.getProduct().getName());
        dto.setProductImageUrl(wishlist.getProduct().getMainImageUrl());
        dto.setCreatedAt(wishlist.getCreatedAt());
        return dto;
    }
}