package com.example.backend.controller.payment;

import com.example.backend.dto.payment.BalanceResponseDto;
import com.example.backend.dto.payment.PointChargeRequestDto;
import com.example.backend.dto.payment.PointRefundRequestDto;
import com.example.backend.service.PaypayService;
import com.example.backend.service.PointService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/api/balances")
public class UserBalanceController {

    private final PointService pointService;
    private final PaypayService paypayService;

    @Autowired
    public UserBalanceController(PointService pointService, PaypayService paypayService) {
        this.pointService = pointService;
        this.paypayService = paypayService;
    }

    @GetMapping("/{userId}")
    public ResponseEntity<BalanceResponseDto> getBalances(@PathVariable String userId) {
        int pointBalance = pointService.getPointBalance(userId);
        int paypayBalance = paypayService.getPaypayBalance(userId);
        return ResponseEntity.ok(new BalanceResponseDto(pointBalance, paypayBalance));
    }

    @PostMapping("/charge/card")
    public ResponseEntity<Map<String, ?>> chargePointsWithCreditCard(@Valid @RequestBody PointChargeRequestDto requestDto) {
        try {
            int newBalance = pointService.chargePointsWithCreditCard(requestDto);
            return ResponseEntity.ok(Map.of("message", "クレジットカードでポイントがチャージされました。", "newPointBalance", newBalance));
        } catch (IllegalArgumentException | EntityNotFoundException e) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", e.getMessage()));
        }
    }

    @PostMapping("/charge/paypay")
    public ResponseEntity<Map<String, ?>> chargePointsWithPaypay(@Valid @RequestBody PointChargeRequestDto requestDto) {
        try {
            BalanceResponseDto balances = pointService.chargePointsWithPaypay(requestDto);
            return ResponseEntity.ok(Map.of("message", "PayPayでポイントがチャージされました。", "balances", balances));
        } catch (IllegalArgumentException | IllegalStateException | EntityNotFoundException e) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", e.getMessage()));
        }
    }

    @PostMapping("/refund/paypay")
    public ResponseEntity<Map<String, ?>> refundPointsToPaypay(@Valid @RequestBody PointRefundRequestDto requestDto) {
        try {
            BalanceResponseDto balances = pointService.refundPointsToPaypay(requestDto);
            return ResponseEntity.ok(Map.of("message", "ポイントがPayPayに払い戻されました。", "balances", balances));
        } catch (IllegalArgumentException | IllegalStateException | EntityNotFoundException e) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", e.getMessage()));
        }
    }
}
