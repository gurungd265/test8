package com.example.backend.repository.cart;

import com.example.backend.entity.cart.Cart;
import com.example.backend.entity.cart.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    // 카트 목록 + 상품 속성 조회 (기존 메서드)
    @Query("SELECT ci FROM CartItem ci " +
            "LEFT JOIN FETCH ci.options cic " +
            "LEFT JOIN FETCH cic.productOption " +
            "WHERE ci.cart.id = :cartId AND ci.deletedAt IS NULL")
    List<CartItem> findByCartIdWithoptions(@Param("cartId") Long cartId);

    // 특정 상품(Product) ID에 해당하는 모든 CartItem을 소프트 삭제
    @Modifying
    @Query("UPDATE CartItem ci SET ci.deletedAt = CURRENT_TIMESTAMP WHERE ci.product.id = :productId AND ci.deletedAt IS NULL")
    void softDeleteByProductId(@Param("productId") Long productId);

    // 필요하다면 특정 Cart와 Product에 대한 CartItem을 조회
    Optional<CartItem> findByCartAndProductIdAndDeletedAtIsNull(Cart cart, Long productId);

    // [추가] 배치 삭제 시 활성 아이템만 조회
    List<CartItem> findByCartAndIdInAndDeletedAtIsNull(Cart cart,List<Long> ids);

    // [추가] CartId 기반으로 활성 아이템만 조회 (fetch join 없이)
    List<CartItem> findByCartIdAndDeletedAtIsNull(Long cartId);
}
