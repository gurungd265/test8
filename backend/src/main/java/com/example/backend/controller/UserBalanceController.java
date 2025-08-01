package com.example.backend.controller;

import com.example.backend.dto.user.BalanceTransactionRequestDto;
import com.example.backend.entity.payment.PaymentMethod;
import com.example.backend.entity.user.UserBalance;
import com.example.backend.service.UserBalanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/balances")
public class UserBalanceController {

    private final UserBalanceService userBalanceService;

    @Autowired
    public UserBalanceController(UserBalanceService userBalanceService){
        this.userBalanceService = userBalanceService;
    }

    @GetMapping("/find")
    public ResponseEntity<UserBalance> findBalance(@RequestParam String userId, @RequestParam PaymentMethod paymentMethod){
        return userBalanceService.findUserBalance(userId, paymentMethod)
                .map(ResponseEntity::ok)
                .orElseGet(()->ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PostMapping("/Charge")
    public ResponseEntity<UserBalance> chargeBalance(@RequestParam String userId,@RequestParam PaymentMethod paymentMethod,int amount){
        try{
            UserBalance updateBalance = userBalanceService.chargeBalance(userId,paymentMethod,amount);
            return ResponseEntity.ok(updateBalance);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/debit")
    public  ResponseEntity<?> debitBalance(@RequestBody BalanceTransactionRequestDto request){
        try{
            UserBalance updateBalance = userBalanceService.debitBalance(
                    request.getUserId(),
                    request.getPaymentMethod(),
                    request.getAmount()
            );
            return ResponseEntity.ok(updateBalance);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
