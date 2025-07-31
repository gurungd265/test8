package com.example.backend.repository;

import com.example.backend.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    // 특정 Cart에 속한 모든 CartItem을 조회 (deleted_at이 null인 것만)
    List<CartItem> findByCartIdAndDeletedAtIsNull(Long cartId);

    // 특정 상품(Product) ID에 해당하는 모든 CartItem을 소프트 삭제
    // 상품이 삭제될 때 해당 상품을 담고 있는 모든 장바구니 아이템을 비활성화 (삭제)하는 기능입니다.
    @Modifying // 데이터 변경 쿼리임을 나타냄
    @Query("UPDATE CartItem ci SET ci.deletedAt = CURRENT_TIMESTAMP WHERE ci.product.id = :productId AND ci.deletedAt IS NULL")
    void softDeleteByProductId(@Param("productId") Long productId);

    // 필요하다면 특정 Cart와 Product에 대한 CartItem을 조회
    Optional<CartItem> findByCartAndProductIdAndDeletedAtIsNull(com.example.backend.entity.Cart cart, Long productId);

    // 기타 필요한 CartItem 관련 조회, 삭제 메서드들을 추가할 수 있습니다.
    // 예: 특정 카트 아이템을 ID로 찾기 (소프트 삭제되지 않은 것만)
    Optional<CartItem> findByIdAndDeletedAtIsNull(Long id);
}