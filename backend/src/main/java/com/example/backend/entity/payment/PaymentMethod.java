package com.example.backend.entity.payment;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum PaymentMethod {
    CREDIT_CARD,
    KONBINI,            // CONVENIENCE_STORE
    BANK_TRANSFER,
    COD;                 // Cash On Delivery

    @JsonCreator
    public static PaymentMethod from(String value) {
        return value == null ? null : PaymentMethod.valueOf(value.toUpperCase());
    }

}