package com.example.backend.dto.payment;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PointChargeRequestDto {
    private String userId;
    private int amount;
}
