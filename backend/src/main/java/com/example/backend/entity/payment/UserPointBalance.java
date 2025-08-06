package com.example.backend.entity.payment;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "user_point_balances")
public class UserPointBalance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String userId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balance;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.balance == null) {
            this.balance = BigDecimal.ZERO;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void addBalance(BigDecimal amount) {
        if (BigDecimal.ZERO.compareTo(amount) > 0) { // amount < 0 の場合
            throw new IllegalArgumentException("追加金額は0円以上から");
        }
        this.balance = this.balance.add(amount); // BigDecimal の加算演算
    }

    public void subtractBalance(BigDecimal amount) {
        if (BigDecimal.ZERO.compareTo(amount) > 0) { // amount < 0 の場合
            throw new IllegalArgumentException("差し引かれる金額は、0円以上から.");
        }
        if (this.balance.compareTo(amount) < 0) { // 残高が不足している場合
            throw new IllegalStateException("残高が不足しています。");
        }
        this.balance = this.balance.subtract(amount);
    }
}
