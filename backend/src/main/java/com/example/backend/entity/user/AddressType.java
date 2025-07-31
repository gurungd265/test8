package com.example.backend.entity.user;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum AddressType {
    HOME,
    WORK;

    @JsonCreator
    public static AddressType from(String value) {
        if (value == null) return null;
        try {
            return AddressType.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            // 기본값 지정하거나 null 반환
            return null;
        }
    }
}
