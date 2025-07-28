package com.example.backend.repository;

import com.example.backend.entity.Order;
import com.example.backend.entity.Payment;
import com.example.backend.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    // 트랜잭션 고유번호로 단건 결제 조회
    Optional<Payment> findByTransactionId(String transactionId);

    // 주문별 결제내역 목록 조회
    List<Payment> findByOrder(Order order);

    // 특정 상태의 결제내역 목록 조회
    List<Payment> findByStatus(PaymentStatus status);

}
