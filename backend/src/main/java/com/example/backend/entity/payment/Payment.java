package com.example.backend.entity.payment;

import com.example.backend.entity.order.Order;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.Where;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "payments",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_payment_transaction_id",
                        columnNames = "transaction_id"
                )
        }
)
@Where(clause = "deleted_at IS NULL")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)  // 각각의 결제는 하나의 주문에 속해있음
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "user_id",nullable = false)
    private String userId;

    @Column(nullable = false)
    private BigDecimal amount; // 개별 결제 건별 금액

    @Column(name = "refund_amount", nullable = true)
    private BigDecimal refundAmount;  // 환불된 금액을 저장

    @Column(name = "card_amount", nullable = true,precision = 19,scale = 2)
    private BigDecimal cardAmount;

    /**
     * 복수결제 허용 시 별도 관리 테이블 필요 (ex: 카드 + 포인트 결제 -> 포인트 관리 테이블 필요)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", length = 50)
    private PaymentMethod paymentMethod; // 결제 수단 정보

    @Column(name = "transaction_id")
    private String transactionId;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = true, updatable = true)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at", nullable = true, updatable = true)
    private LocalDateTime deletedAt;

}
