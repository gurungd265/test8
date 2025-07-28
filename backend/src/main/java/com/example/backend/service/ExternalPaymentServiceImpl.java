package com.example.backend.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@Slf4j
public class ExternalPaymentServiceImpl implements ExternalPaymentService {

    @Override
    public boolean charge(String paymentMethod, BigDecimal amount, String transactionId) {
        log.info("外部決済リクエスト: paymentMethod={}, amount={}, transactionId={}", paymentMethod, amount, transactionId);
        return true; // 모킹: 무조건 성공 처리
    }

    @Override
    public boolean cancel(String transactionId) {
        log.info("外部決済取り消しリクエスト: transactionId={}", transactionId);
        return true; // 모킹: 무조건 성공 처리
    }

    @Override
    public boolean refund(String transactionId, BigDecimal amount) {
        log.info("外部返金リクエスト: transactionId={}, amount={}", transactionId, amount);
        return true; // 모킹: 무조건 성공 처리
    }

}