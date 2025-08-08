package com.example.backend.controller.payment;

import com.example.backend.dto.payment.PaypayRegistrationRequestDto;
import com.example.backend.entity.payment.PaypayAccount;
import com.example.backend.service.payment.PaypayService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/paypay/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PaypayAccount> getPaypayAccount(@PathVariable String userId) {
        return paypayService.getPaypayAccountByUserId(userId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/paypay/topup")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, ?>> topUpPaypayBalance(@RequestBody Map<String, Object> payload) {
        String userId = (String) payload.get("userId");
        Integer amount = (Integer) payload.get("amount");
        if (userId == null || amount == null) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", "ユーザーIDと金額は必須です。"));
        }
        try {
            PaypayAccount updatedAccount = paypayService.topUpPaypayBalance(userId, amount);
            return ResponseEntity.ok(Map.of("message", "PayPay残高が正常にチャージされました。", "newBalance", updatedAccount.getBalance()));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.singletonMap("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", e.getMessage()));
        }
    }
}
