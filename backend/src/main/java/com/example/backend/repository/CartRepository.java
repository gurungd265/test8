package com.example.backend.repository;

import com.example.backend.entity.Cart;
import com.example.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart,Long> {
    /*
        (deleted_at IS NULL 자동 필터링)
     */
    // 회원 카트 조회
    Optional<Cart> findByUser(User user);
    // 비회원 카트 조회
    Optional<Cart> findBySessionId(String sessionId);
}
