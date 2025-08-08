package com.example.backend.service.payment;

import java.math.BigDecimal;

public interface ExternalPaymentService {

    boolean charge(String paymentMethod, BigDecimal amount, String transactionId);
    boolean cancel(String transactionId);
    boolean refund(String transactionId, BigDecimal amount); // 추가

}