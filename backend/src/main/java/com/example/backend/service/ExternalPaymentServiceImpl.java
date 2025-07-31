package com.example.backend.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@Slf4j
public class ExternalPaymentServiceImpl implements ExternalPaymentService {

    @Override
    public boolean charge(String paymentMethod, BigDecimal amount, String transactionId) {
        switch (paymentMethod.toLowerCase()) {
            case "credit_card":
                // 신용카드 결제 모킹 로직
                System.out.println("Processing credit card payment: " + amount);
                return true; // 결제 성공 모킹
            case "konbini":
                // 편의점 결제 모킹 로직
                System.out.println("Processing konbini payment: " + amount);
                return true; // 결제 성공 모킹
            case "bank_transfer":
                // 계좌이체 모킹 로직
                System.out.println("Processing bank transfer payment: " + amount);
                return true; // 결제 성공 모킹
            case "cod":
                // 현금 착불 모킹 로직
                System.out.println("Processing Cash On Delivery payment: " + amount);
                return true; // 결제 성공 모킹
            default:
                System.out.println("Unknown payment method: " + paymentMethod);
                return false; // 실패 처리
        }
    }

    @Override
    public boolean cancel(String transactionId) {
        // 결제 취소 모킹 처리
        System.out.println("Canceling payment: " + transactionId);
        return true;
    }

    @Override
    public boolean refund(String transactionId, BigDecimal amount) {
        // 환불 모킹 처리
        System.out.println("Refunding payment: " + transactionId + ", amount: " + amount);
        return true;
    }

}