package com.example.backend.repository.cart;

import com.example.backend.entity.cart.CartItemOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CartItemOptionRepository extends JpaRepository<CartItemOption,Long> {
    /*
        (deleted_at IS NULL 자동 필터링)
     */
    List<CartItemOption> findByCartItemId(Long cartItemId);
}
