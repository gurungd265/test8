package com.example.backend.repository;

import com.example.backend.entity.Cart;
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
    /*
        (deleted_at IS NULL 자동 필터링)
     */

    // 카트 목록 + 상품 속성 조회
    @Query("SELECT ci FROM CartItem ci " +
            "LEFT JOIN FETCH ci.options cic " +
            "LEFT JOIN FETCH cic.productOption " +
            "WHERE ci.cart.id = :cartId AND ci.deletedAt IS NULL")
    List<CartItem> findByCartIdWithoptions(@Param("cartId") Long cartId);

    // 특정 상품(Product) ID에 해당하는 모든 CartItem을 소프트 삭제
    // 상품이 삭제될 때 해당 상품을 담고 있는 모든 장바구니 아이템을 비활성화 (삭제)하는 기능입니다.
    @Modifying
    @Query("UPDATE CartItem ci SET ci.deletedAt = CURRENT_TIMESTAMP WHERE ci.product.id = :productId AND ci.deletedAt IS NULL")
    void softDeleteByProductId(@Param("productId") Long productId);

    // 필요하다면 특정 Cart와 Product에 대한 CartItem을 조회
    Optional<CartItem> findByCartAndProductIdAndDeletedAtIsNull(com.example.backend.entity.Cart cart, Long productId);

    List<CartItem> findByCartAndIdIn(Cart cart,List<Long> ids);

}