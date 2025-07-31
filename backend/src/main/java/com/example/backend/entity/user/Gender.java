package com.example.backend.entity.user;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum Gender {
    MALE,
    FEMALE,
    OTHER;

    @JsonCreator
    public static Gender from(String value) {
        return value == null ? null : Gender.valueOf(value.toUpperCase());
    }
}
