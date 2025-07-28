package com.example.backend.repository;

import com.example.backend.entity.order.Order;
import com.example.backend.entity.order.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    /*
        (deleted_at IS NULL 자동 필터링)
     */

    // 특정 사용자의 이메일로 주문 목록 조회
    List<Order> findByUserEmail(String userEmail);

    // 특정 ID와 사용자 이메일로 단건 주문 조회
    Optional<Order> findByIdAndUserEmail(Long id, String email);

    // 주문상태별 주문 목록 조회
    List<Order> findByStatus(OrderStatus status);

    // 주문번호별 단건 주문 조회
    Optional<Order> findByOrderNumber(String orderNumber);
}