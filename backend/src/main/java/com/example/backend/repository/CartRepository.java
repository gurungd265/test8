package com.example.backend.repository;

import com.example.backend.entity.Cart;
import com.example.backend.entity.user.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart,Long> {
    /*
        (deleted_at IS NULL 자동 필터링)
     */
    Optional<Cart> findByUser(User user); // 회원 카트 조회
    Optional<Cart> findBySessionId(String sessionId); // 비회원 카트 조회

    @Modifying
    @Query(value = "DELETE FROM Cart c WHERE c.product.id = :productId")
    void deleteByProductId(@Param("productId") Long productId); // 상품이 삭제된 경우 카트에서도 삭제
}
