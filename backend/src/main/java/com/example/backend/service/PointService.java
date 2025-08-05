package com.example.backend.service;

import com.example.backend.dto.payment.BalanceResponseDto;
import com.example.backend.dto.payment.PointChargeRequestDto;
import com.example.backend.dto.payment.PointRefundRequestDto;
import com.example.backend.entity.payment.PaypayAccount;
import com.example.backend.entity.payment.UserPointBalance;
import com.example.backend.repository.payment.PaypayAccountRepository;
import com.example.backend.repository.payment.UserPointBalanceRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PointService {

    @Autowired
    private UserPointBalanceRepository userPointBalanceRepository;

    @Autowired
    private PaypayAccountRepository paypayAccountRepository;

    public int getPointBalance(String userId) {
        return userPointBalanceRepository.findByUserId(userId)
                .map(UserPointBalance::getBalance)
                .orElse(0);
    }

    @Transactional
    public int chargePointsWithCreditCard(PointChargeRequestDto requestDto) {
        if (requestDto.getAmount() <= 0) {
            throw new IllegalArgumentException("チャージ金額は0より大きくなければなりません。");
        }

        UserPointBalance pointBalance = userPointBalanceRepository.findByUserId(requestDto.getUserId())
                .orElseGet(() -> {
                    UserPointBalance newPointBalance = new UserPointBalance();
                    newPointBalance.setUserId(requestDto.getUserId());
                    newPointBalance.setBalance(0);
                    return userPointBalanceRepository.save(newPointBalance);
                });
        pointBalance.addBalance(requestDto.getAmount());
        userPointBalanceRepository.save(pointBalance);

        return pointBalance.getBalance();
    }

    @Transactional
    public BalanceResponseDto chargePointsWithPaypay(PointChargeRequestDto requestDto) {
        if (requestDto.getAmount() <= 0) {
            throw new IllegalArgumentException("チャージ金額は0より大きくなければなりません。");
        }

        PaypayAccount paypayAccount = paypayAccountRepository.findByUserId(requestDto.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("PayPayアカウントが見つかりません。"));

        UserPointBalance pointBalance = userPointBalanceRepository.findByUserId(requestDto.getUserId())
                .orElseGet(() -> {
                    UserPointBalance newPointBalance = new UserPointBalance();
                    newPointBalance.setUserId(requestDto.getUserId());
                    newPointBalance.setBalance(0);
                    return userPointBalanceRepository.save(newPointBalance);
                });

        paypayAccount.subtractBalance(requestDto.getAmount());
        pointBalance.addBalance(requestDto.getAmount());

        paypayAccountRepository.save(paypayAccount);
        userPointBalanceRepository.save(pointBalance);

        return new BalanceResponseDto(pointBalance.getBalance(), paypayAccount.getBalance());
    }

    @Transactional
    public BalanceResponseDto refundPointsToPaypay(PointRefundRequestDto requestDto) {
        if (requestDto.getAmount() <= 0) {
            throw new IllegalArgumentException("払い戻し金額は0より大きくなければなりません。");
        }

        UserPointBalance pointBalance = userPointBalanceRepository.findByUserId(requestDto.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("ポイント残高が見つかりません。"));

        PaypayAccount paypayAccount = paypayAccountRepository.findByUserId(requestDto.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("PayPayアカウントが見つかりません。"));

        pointBalance.subtractBalance(requestDto.getAmount());
        paypayAccount.addBalance(requestDto.getAmount());

        userPointBalanceRepository.save(pointBalance);
        paypayAccountRepository.save(paypayAccount);

        return new BalanceResponseDto(pointBalance.getBalance(), paypayAccount.getBalance());
    }


}