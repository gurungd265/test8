package com.example.backend.entity.user;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum UserType {
    USER,
    ADMIN;

    @JsonCreator
    public static UserType from(String value) {
        return value == null ? null : UserType.valueOf(value.toUpperCase());
    }
}
