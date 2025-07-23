package com.example.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Where;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "cart_items")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Where(clause = "deleted_at IS NULL") //기본 조회 시 자동으로 삭제된 데이터는 제외
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id")
    private Cart cart;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(nullable = false)
    @Min(1)
    private Integer quantity;

    @Column(name = "price_at_addition") // 소수점 자리수 설정 원하면 precision = 10, scale = 2
    private BigDecimal priceAtAddition; //장바구니에 상품을 담은 시점의 가격

    @CreationTimestamp
    @Column(name = "added_at", nullable = false, updatable = false)
    private LocalDateTime addedAt; // 장바구니에 상품을 담은 시점

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    //expireAt 장바구니 유효기간 기능 추가...?
}
