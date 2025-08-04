package com.example.backend.repository.payment;

import com.example.backend.entity.payment.PaypayAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaypayAccountRepository extends JpaRepository<PaypayAccount, Long> {
    Optional<PaypayAccount> findByUserId(String userId);
}
