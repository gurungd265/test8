package com.example.backend.entity.user;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum AddressType {
    HOME,
    WORK;

    @JsonCreator
    public static AddressType from(String value) {
        return value == null ? null : AddressType.valueOf(value.toUpperCase());
    }
}
