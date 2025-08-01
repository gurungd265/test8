package com.example.backend.entity.user;

import com.example.backend.entity.payment.PaymentMethod;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_balance")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserBalance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private PaymentMethod paymentMethod;

    @Column(name = "balance", nullable = false)
    private int balance;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public void addBalance(int amount) {
        this.balance += amount;
        this.updatedAt = LocalDateTime.now();
    }

    public void subtractBalance(int amount) {
        if (this.balance < amount) {
            throw new IllegalStateException("残高が不足しています。");
        }
        this.balance -= amount;
        this.updatedAt = LocalDateTime.now();
    }
}
