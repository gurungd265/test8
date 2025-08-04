package com.example.backend.controller.payment;

import com.example.backend.dto.payment.CardRegistrationRequestDto;
import com.example.backend.entity.payment.Card;
import com.example.backend.service.CardService;
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
}