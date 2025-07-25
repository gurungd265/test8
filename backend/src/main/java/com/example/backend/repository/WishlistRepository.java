package com.example.backend.repository;

import com.example.backend.entity.Wishlist;
import com.example.backend.entity.user.User;
import com.example.backend.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WishlistRepository extends JpaRepository<Wishlist, Long> {
    /*
        (deleted_at IS NULL 자동 필터링)
     */

    // 특정 유저의 위시리스트 목록 조회
    List<Wishlist> findByUserId(Long userId);

    // 특정 위시리스트 항목을 유저 기준으로 안전하게 조회
    Optional<Wishlist> findByIdAndUserId(Long wishlistId, Long userId);

    // 특정 상품이 존재하는지 조회 (소프트 삭제 여부)
    List<Wishlist> findByProductId(Long productId);

    // 특정 유저가 해당 특정 상품을 위시리스트에 추가했는지 체크
    boolean existsByUserAndProduct(User user, Product product);

}