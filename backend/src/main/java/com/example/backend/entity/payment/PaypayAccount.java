package com.example.backend.entity.payment;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "paypay_accounts")
public class PaypayAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String userId;

    @Column(nullable = false, unique = true)
    private String paypayId;

    @Column(nullable = false)
    private int balance;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void addBalance(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("追加金額は0円以上から");
        }
        this.balance += amount;
    }

    public void subtractBalance(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("差し引かれる金額は、0円以上から.");
        }
        if (this.balance < amount) {
            throw new IllegalStateException("残高が不足しています。");
        }
        this.balance -= amount;
    }
}
