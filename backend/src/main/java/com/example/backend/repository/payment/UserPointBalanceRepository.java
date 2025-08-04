package com.example.backend.repository.payment;

import com.example.backend.entity.payment.UserPointBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserPointBalanceRepository extends JpaRepository<UserPointBalance, Long> {
    Optional<UserPointBalance> findByUserId(String userId);
}