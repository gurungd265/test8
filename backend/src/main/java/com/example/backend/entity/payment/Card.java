package com.example.backend.entity.payment;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "card")
public class Card {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String userId;

    @Column(nullable = false)
    private String cardCompanyName;

    @Column(nullable = false)
    private String maskedCardNumber;

    @Column(nullable = false)
    private String cardHolderName;

    @Column(nullable = false)
    private String expiryDate;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal availableCredit;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.availableCredit == null) {
            this.availableCredit = BigDecimal.ZERO; // 초기 잔액을 0으로 설정
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void addCredit(BigDecimal amount) {
        // 금액이 0보다 큰지 BigDecimal의 compareTo 메서드로 확인
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("チャージ金額は0より大きくなければなりません。");
        }
        this.availableCredit = this.availableCredit.add(amount);
    }


    public void subtractCredit(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("減額金額は0より大きくなければなりません。");
        }

        if (this.availableCredit.compareTo(amount) < 0) {
            throw new IllegalStateException("利用可能残高が足りません。");
        }
        this.availableCredit = this.availableCredit.subtract(amount);
    }
}
