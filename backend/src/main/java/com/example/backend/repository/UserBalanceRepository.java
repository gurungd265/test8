package com.example.backend.repository;

import com.example.backend.entity.payment.PaymentMethod;
import com.example.backend.entity.user.UserBalance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserBalanceRepository extends JpaRepository<UserBalance,Long> {

    Optional<UserBalance> findByUserIdAndPaymentMethod(String userId, PaymentMethod paymentMethod);
}
