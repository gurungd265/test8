package com.example.backend.entity.payment;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum PaymentMethod {
    POINT,
    PAYPAY,
    CREDIT_CARD;

    @JsonCreator
    public static PaymentMethod from(String value) {
        return value == null ? null : PaymentMethod.valueOf(value.toUpperCase());
    }

}