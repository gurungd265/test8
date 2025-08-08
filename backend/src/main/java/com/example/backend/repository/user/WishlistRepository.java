package com.example.backend.repository.user;

import com.example.backend.entity.user.Wishlist;
import com.example.backend.entity.user.User;
import com.example.backend.entity.product.Product;
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

    // 특정 유저의 위시리스트 항목이 존재하는지 확인 (소프트 삭제되지 않은 것만)
    boolean existsByUserAndProductAndDeletedAtIsNull(User user, Product product);

    // 특정 유저가 해당 특정 상품을 위시리스트에 추가했는지 체크
    boolean existsByUserAndProduct(User user, Product product);

    // ⭐ 새로 추가할 쿼리 메소드: userId와 productId로 위시리스트 항목 조회
    Optional<Wishlist> findByUserIdAndProductIdAndDeletedAtIsNull(Long userId, Long productId);

    // 위시리스트 전체 조회 시 정렬을 위해 필요할 수 있음
    List<Wishlist> findByUserIdAndDeletedAtIsNull(Long userId); // getWishlistByUser에서 사용될 수 있음

}