package com.example.backend.repository;

import com.example.backend.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    /*
        (deleted_at IS NULL 자동 필터링)
     */
    List<Order> findByUserEmail(String userEmail);
    Optional<Order> findByIdAndUserEmail(Long id, String email);
}