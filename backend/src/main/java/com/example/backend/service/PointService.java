package com.example.backend.service;

import com.example.backend.dto.payment.BalanceResponseDto;
import com.example.backend.dto.payment.PointChargeRequestDto;
import com.example.backend.dto.payment.PointRefundRequestDto;
import com.example.backend.entity.payment.PaypayAccount;
import com.example.backend.entity.payment.UserPointBalance;
import com.example.backend.repository.payment.PaypayAccountRepository;
import com.example.backend.repository.payment.UserPointBalanceRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class PointService {

    @Autowired
    private UserPointBalanceRepository userPointBalanceRepository;

    @Autowired
    private PaypayAccountRepository paypayAccountRepository;

    public BigDecimal getPointBalance(String userId) {
        return userPointBalanceRepository.findByUserId(userId)
                .map(UserPointBalance::getBalance)
                .orElse(BigDecimal.ZERO); // 見つからない場合は 0 を返す
    }

    @Transactional
    public BigDecimal chargePointsWithCreditCard(PointChargeRequestDto requestDto) { // BigDecimal を返すように変更
        BigDecimal amount = BigDecimal.valueOf(requestDto.getAmount()); // int から BigDecimal へ変換
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("チャージ金額は0より大きくなければなりません。");
        }

        UserPointBalance pointBalance = userPointBalanceRepository.findByUserId(requestDto.getUserId())
                .orElseGet(() -> {
                    UserPointBalance newPointBalance = new UserPointBalance();
                    newPointBalance.setUserId(requestDto.getUserId());
                    newPointBalance.setBalance(BigDecimal.ZERO); // BigDecimal.ZERO で初期化
                    return userPointBalanceRepository.save(newPointBalance);
                });
        pointBalance.addBalance(amount); //  BigDecimal を渡す
        userPointBalanceRepository.save(pointBalance);

        return pointBalance.getBalance(); // BigDecimal を返す
    }

    @Transactional
    public BalanceResponseDto chargePointsWithPaypay(PointChargeRequestDto requestDto) {
        BigDecimal amount = BigDecimal.valueOf(requestDto.getAmount());
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("チャージ金額は0より大きくなければなりません。");
        }

        PaypayAccount paypayAccount = paypayAccountRepository.findByUserId(requestDto.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("PayPayアカウントが見つかりません。"));

        UserPointBalance pointBalance = userPointBalanceRepository.findByUserId(requestDto.getUserId())
                .orElseGet(() -> {
                    UserPointBalance newPointBalance = new UserPointBalance();
                    newPointBalance.setUserId(requestDto.getUserId());
                    newPointBalance.setBalance(BigDecimal.ZERO);
                    return userPointBalanceRepository.save(newPointBalance);
                });

        paypayAccount.subtractBalance(amount);
        pointBalance.addBalance(amount);

        paypayAccountRepository.save(paypayAccount);
        userPointBalanceRepository.save(pointBalance);

        // BalanceResponseDto のコンストラクタに合わせて BigDecimal を渡す
        return new BalanceResponseDto(pointBalance.getBalance(), paypayAccount.getBalance());
    }

    @Transactional
    public BalanceResponseDto refundPointsToPaypay(PointRefundRequestDto requestDto) {
        BigDecimal amount = BigDecimal.valueOf(requestDto.getAmount()); // int から BigDecimal へ変換
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("払い戻し金額は0より大きくなければなりません。");
        }

        UserPointBalance pointBalance = userPointBalanceRepository.findByUserId(requestDto.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("ポイント残高が見つかりません。"));

        PaypayAccount paypayAccount = paypayAccountRepository.findByUserId(requestDto.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("PayPayアカウントが見つかりません。"));

        pointBalance.subtractBalance(amount); // BigDecimal を渡す
        paypayAccount.addBalance(amount); // BigDecimal を渡す

        userPointBalanceRepository.save(pointBalance);
        paypayAccountRepository.save(paypayAccount);

        // BalanceResponseDto のコンストラクタに合わせて BigDecimal を渡す
        return new BalanceResponseDto(pointBalance.getBalance(), paypayAccount.getBalance());
    }

    // deductPoints が BigDecimal amount を受け取るように変更
    @Transactional
    public void deductPoints(String userId, BigDecimal amount) { // BigDecimal amount を受け取る
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("差し引くポイント数は0より大きくなければなりません。");
        }

        UserPointBalance pointBalance = userPointBalanceRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("ユーザーのポイント残高が見つかりません。"));

        // BigDecimal の比較: compareTo() を使用
        if (pointBalance.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("ポイント残高が不足しています。現在の残高: " + pointBalance.getBalance() + "、要求された金額: " + amount);
        }

        pointBalance.subtractBalance(amount); // BigDecimal を渡す
        userPointBalanceRepository.save(pointBalance);
    }
}
