package com.example.backend.controller.payment;

import com.example.backend.dto.payment.CardRegistrationRequestDto;
import com.example.backend.entity.payment.Card;
import com.example.backend.service.CardService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/api/register")
public class CardRegistrationController {

    private final CardService cardService;

    @Autowired
    public CardRegistrationController(CardService cardService) {
        this.cardService = cardService;
    }

    @PostMapping("/card")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, ?>> registerCard(@Valid @RequestBody CardRegistrationRequestDto requestDto) {
        try {
            Card newCard = cardService.registerCard(requestDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(Collections.singletonMap("message", "クレジットカードが正常に登録されました。"));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", e.getMessage()));
        }
    }

    @GetMapping("/card/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Card> getCard(@PathVariable String userId) {
        return cardService.getCardByUserId(userId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/card/topup")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, ?>> topUpCardBalance(@RequestBody Map<String, Object> payload) {
        String userId = (String) payload.get("userId");
        // 요청 바디에서 amount를 Integer 대신 Number로 받고, BigDecimal로 변환합니다.
        Number amountNumber = (Number) payload.get("amount");

        if (userId == null || amountNumber == null) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", "ユーザーIDと金額は必須です。"));
        }

        try {
            // Number 타입을 BigDecimal로 변환하여 서비스에 전달합니다.
            BigDecimal amount = BigDecimal.valueOf(amountNumber.doubleValue());
            Card updatedCard = cardService.topUpCardBalance(userId, amount);
            return ResponseEntity.ok(Map.of("message", "クレジットカード残高が正常にチャージされました。", "newAvailableCredit", updatedCard.getAvailableCredit()));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.singletonMap("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", e.getMessage()));
        }
    }
}
