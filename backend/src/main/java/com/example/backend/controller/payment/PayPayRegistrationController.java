package com.example.backend.controller.payment;

import com.example.backend.dto.payment.PaypayRegistrationRequestDto;
import com.example.backend.entity.payment.PaypayAccount;
import com.example.backend.service.PaypayService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/api/register")
public class PayPayRegistrationController {

    private final PaypayService paypayService;

    @Autowired
    public PayPayRegistrationController(PaypayService paypayService) {
        this.paypayService = paypayService;
    }

    @PostMapping("/paypay")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, ?>> registerPayPay(@Valid @RequestBody PaypayRegistrationRequestDto requestDto) {
        try {
            PaypayAccount newAccount = paypayService.registerPaypayAccount(requestDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(Collections.singletonMap("message", "PayPayアカウントが正常に登録されました。"));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", e.getMessage()));
        }
    }
}
