package com.example.backend.repository.order;

import com.example.backend.entity.order.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    /*
        (deleted_at IS NULL 자동 필터링)
     */
}
