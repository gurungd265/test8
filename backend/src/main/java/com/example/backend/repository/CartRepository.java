package com.example.backend.repository;

import com.example.backend.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CartRepository extends JpaRepository<Cart,Long> {
    /*
        (deleted_at IS NULL 자동 필터링)
     */
}
