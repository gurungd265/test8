package com.example.backend.service;

import com.example.backend.dto.payment.PaypayRegistrationRequestDto;
import com.example.backend.entity.payment.PaypayAccount;
import com.example.backend.repository.payment.PaypayAccountRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PaypayService {

    @Autowired
    private PaypayAccountRepository paypayAccountRepository;

    public int getPaypayBalance(String userId) {
        return paypayAccountRepository.findByUserId(userId)
                .map(PaypayAccount::getBalance)
                .orElse(0);
    }

    @Transactional
    public PaypayAccount registerPaypayAccount(PaypayRegistrationRequestDto requestDto) {
        Optional<PaypayAccount> existingPaypayAccount = paypayAccountRepository.findByUserId(requestDto.getUserId());
        if (existingPaypayAccount.isPresent()) {
            throw new IllegalStateException("すでに登録されているPayPayアカウントがあります。");
        }

        PaypayAccount newPaypayAccount = new PaypayAccount();
        newPaypayAccount.setUserId(requestDto.getUserId());
        newPaypayAccount.setPaypayId(requestDto.getPaypayId());
        newPaypayAccount.setBalance(0);

        return paypayAccountRepository.save(newPaypayAccount);
    }
}