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
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cart_items")
@Where(clause = "deleted_at IS NULL") //기본 조회 시 자동으로 삭제된 데이터는 제외
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id")
    private Cart cart;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product; // 정가 등 상품 정보 포함

    @Column(nullable = false)
    @Min(1)
    private Integer quantity;

    @Column(name = "price_at_addition") // 소수점 자리수 설정 원하면 precision = 10, scale = 2
    private BigDecimal priceAtAddition; // 할인가 (장바구니에 상품을 담긴 시점에 할인 적용된 가격)

    @CreationTimestamp
    @Column(name = "added_at", nullable = false, updatable = false)
    private LocalDateTime addedAt; // 장바구니에 담긴 시간

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt; // 삭제 처리한 시간

    // ============================================ CartItemOption ============================================
    @OneToMany(mappedBy = "cartItem", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItemOption> options = new ArrayList<>();

    public void addOption(ProductOption productOption, String value) {
        // Check if the Option already exists
        for (CartItemOption cic : options) {
            if (cic.getProductOption().getId().equals(productOption.getId())) {
                // If exists, update the value
                cic.setOptionValue(value);
                return; // Exit the method
            }
        }
        // If not exists, add a new Option
        CartItemOption cic = new CartItemOption();
        cic.setCartItem(this);
        cic.setProductOption(productOption);
        cic.setOptionValue(value);
        this.options.add(cic);
    }

}
