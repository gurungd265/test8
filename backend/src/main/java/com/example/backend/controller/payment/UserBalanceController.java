package com.example.backend.controller.payment;

import com.example.backend.dto.payment.BalanceResponseDto;
import com.example.backend.dto.payment.DeductReqDto;
import com.example.backend.dto.payment.PointChargeRequestDto;
import com.example.backend.dto.payment.PointRefundRequestDto;
import com.example.backend.entity.payment.Card;
import com.example.backend.service.payment.CardService;
import com.example.backend.service.payment.PaypayService;
import com.example.backend.service.payment.PointService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/balances")
public class UserBalanceController {

    private final PointService pointService;
    private final PaypayService paypayService;
    private final CardService cardService;

    @Autowired
    public UserBalanceController(PointService pointService, PaypayService paypayService, CardService cardService) {
        this.pointService = pointService;
        this.paypayService = paypayService;
        this.cardService = cardService;
    }

    @GetMapping("/{userId}")
    public ResponseEntity<BalanceResponseDto> getBalances(@PathVariable String userId) {
        BigDecimal pointBalance = pointService.getPointBalance(userId);
        BigDecimal paypayBalance = paypayService.getPaypayBalance(userId);
        Optional<Card> card = cardService.getCardByUserId(userId);
        BigDecimal virtualCardBalance = card.map(Card::getAvailableCredit).orElse(BigDecimal.ZERO);

        return ResponseEntity.ok(new BalanceResponseDto(pointBalance, paypayBalance, virtualCardBalance));
    }

    @PostMapping("/charge/card")
    public ResponseEntity<Map<String, ?>> chargePointsWithCreditCard(@Valid @RequestBody PointChargeRequestDto requestDto) {
        try {
            BigDecimal newBalance = pointService.chargePointsWithCreditCard(requestDto);
            return ResponseEntity.ok(Map.of("message", "クレジットカードでポイントがチャージされました。", "newPointBalance", newBalance));
        } catch (IllegalArgumentException | EntityNotFoundException e) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", e.getMessage()));
        }
    }

    @PostMapping("/charge/paypay")
    public ResponseEntity<BalanceResponseDto> chargePointsWithPaypay(@Valid @RequestBody PointChargeRequestDto requestDto) {
        try {
            BalanceResponseDto balances = pointService.chargePointsWithPaypay(requestDto);
            return ResponseEntity.ok(balances);
        } catch (IllegalArgumentException | IllegalStateException | EntityNotFoundException e) {
            return ResponseEntity.badRequest().body(new BalanceResponseDto(null, null, null));
        }
    }

    @PostMapping("/refund/paypay")
    public ResponseEntity<BalanceResponseDto> refundPointsToPaypay(@Valid @RequestBody PointRefundRequestDto requestDto) {
        try {
            BalanceResponseDto balances = pointService.refundPointsToPaypay(requestDto);
            return ResponseEntity.ok(balances);
        } catch (IllegalArgumentException | IllegalStateException | EntityNotFoundException e) {
            return ResponseEntity.badRequest().body(new BalanceResponseDto(null, null, null));
        }
    }

    @PostMapping("/deduct/point")
    public ResponseEntity<Map<String, ?>> deductPoints(@Valid @RequestBody DeductReqDto requestDto) {
        try {
            pointService.deductPoints(requestDto.getUserId(), requestDto.getAmount());
            BigDecimal newBalance = pointService.getPointBalance(requestDto.getUserId());
            return ResponseEntity.ok(Map.of("message", "ポイントが差し引かれました。", "newPointBalance", newBalance));
        } catch (IllegalArgumentException | EntityNotFoundException e) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", e.getMessage()));
        }
    }

    @PostMapping("/deduct/paypay")
    public ResponseEntity<Map<String, ?>> deductPaypayBalance(@Valid @RequestBody DeductReqDto requestDto) {
        try {
            BigDecimal newBalance = paypayService.deductPaypayBalance(requestDto.getUserId(), requestDto.getAmount());
            return ResponseEntity.ok(Map.of("message", "PayPay残高が差し引かれました。", "newPaypayBalance", newBalance));
        } catch (IllegalArgumentException | IllegalStateException | EntityNotFoundException e) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", e.getMessage()));
        }
    }

    @PostMapping("/deduct/card")
    public ResponseEntity<Map<String, ?>> deductVirtualCardBalance(@Valid @RequestBody DeductReqDto requestDto) {
        try {
            BigDecimal newBalance = cardService.deductCreditBalance(requestDto.getUserId(), requestDto.getAmount());
            return ResponseEntity.ok(Map.of("message", "クレジットカード残高が差し引かれました。", "newVirtualCardBalance", newBalance));
        } catch (IllegalArgumentException | IllegalStateException | EntityNotFoundException e) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", e.getMessage()));
        }
    }
}
