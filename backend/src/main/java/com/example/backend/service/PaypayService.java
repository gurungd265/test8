package com.example.backend.service;

import com.example.backend.dto.payment.PaypayRegistrationRequestDto;
import com.example.backend.entity.payment.PaypayAccount;
import com.example.backend.repository.payment.PaypayAccountRepository;
import jakarta.persistence.EntityNotFoundException;
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

    public Optional<PaypayAccount> getPaypayAccountByUserId(String userId){
        return paypayAccountRepository.findByUserId(userId);
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

    // PayPayアカウントの残高を仮想的にチャージするメソッド (新規追加)
    @Transactional
    public PaypayAccount topUpPaypayBalance(String userId, int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("チャージ金額は0より大きくなければなりません。");
        }
        PaypayAccount paypayAccount = paypayAccountRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("PayPayアカウントが見つかりません。"));
        paypayAccount.addBalance(amount);
        return paypayAccountRepository.save(paypayAccount);
    }

    @Transactional
    public int deductPaypayBalance(String userId, int amount) {
        PaypayAccount paypayAccount = paypayAccountRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("PayPayアカウントが見つかりませんでした。"));

        if (paypayAccount.getBalance() < amount) {
            throw new IllegalStateException("PayPay残高が不足しています。");
        }

        paypayAccount.setBalance(paypayAccount.getBalance() - amount);
        paypayAccountRepository.save(paypayAccount);

        return paypayAccount.getBalance();
    }

}