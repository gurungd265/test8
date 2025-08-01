package com.example.backend.service;

import com.example.backend.entity.payment.PaymentMethod;
import com.example.backend.entity.user.UserBalance;
import com.example.backend.repository.UserBalanceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class UserBalanceService {
    private final UserBalanceRepository userBalanceRepository;

    @Autowired
    public UserBalanceService(UserBalanceRepository userBalanceRepository){
        this.userBalanceRepository = userBalanceRepository;
    }

    public Optional<UserBalance> findUserBalance(String userId,PaymentMethod paymentMethod){
        return userBalanceRepository.findByUserIdAndPaymentMethod(userId,paymentMethod);
    }

    @Transactional
    public UserBalance chargeBalance(String userId, PaymentMethod paymentMethod, int amount) {
        if (paymentMethod == PaymentMethod.POINT) {
            UserBalance paypayBalance = findUserBalance(userId, PaymentMethod.PAYPAY)
                    .orElseThrow(() -> new IllegalStateException("ペイペイ残高情報がないため、ポイントをチャージすることができません。"));

            if (paypayBalance.getBalance() < amount) {
                throw new IllegalStateException("ペイペイ残高が不足しているため、ポイントをチャージすることができません。");
            }

            paypayBalance.setBalance(paypayBalance.getBalance() - amount);
            paypayBalance.setUpdatedAt(LocalDateTime.now());
            userBalanceRepository.save(paypayBalance);
        }

        UserBalance userBalance = findUserBalance(userId, paymentMethod)
                .orElseGet(() -> {
                    UserBalance newBalance = new UserBalance();
                    newBalance.setUserId(userId);
                    newBalance.setPaymentMethod(paymentMethod);
                    newBalance.setBalance(0);
                    return newBalance;
                });

        userBalance.setBalance(userBalance.getBalance() + amount);
        userBalance.setUpdatedAt(LocalDateTime.now());
        return userBalanceRepository.save(userBalance);
    }

    @Transactional
    public UserBalance debitBalance(String userId,PaymentMethod paymentMethod, int amount){
        UserBalance userBalance = findUserBalance(userId,paymentMethod)
                .orElseThrow(()->new IllegalStateException("残高情報が存在しません。"));

        if(userBalance.getBalance()<amount){
            throw new IllegalStateException("残高が足りないです");
        }

        userBalance.setBalance(userBalance.getBalance()-amount);
        userBalance.setUpdatedAt(LocalDateTime.now());
        return userBalanceRepository.save(userBalance);
    }
}
