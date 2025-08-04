package com.example.backend.dto.payment;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaypayRegistrationRequestDto {
    private String userId;
    private String paypayId;
}
