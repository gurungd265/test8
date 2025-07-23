package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.Where;

import java.util.*;


import java.time.LocalDateTime;

@Entity
@Table(name = "carts", indexes = {
        @Index(name = "idx_session_id", columnList = "session_id")
})
@Data
@AllArgsConstructor
@NoArgsConstructor
@Where(clause = "deleted_at IS NULL") //기본 조회 시 자동으로 삭제된 데이터는 제외
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 회원용 (비회원에서 로그인한 경우에도 연결)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = true)
    private User user;

    // 비회원 세션용
    @Column(name = "session_id", nullable = true)
    private String sessionId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", updatable = true)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at", updatable = true)
    private LocalDateTime deletedAt;

    //============================================= CartItem =============================================
    //카트 삭제 시 CartItem도 자동 삭제
    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true) //LAZY Default
    private List<CartItem> cartItems = new ArrayList<>();

    // CartItem 추가
    public void addCartItem(CartItem item) {
        cartItems.add(item);
        item.setCart(this);
    }

    // CartItem 제거
    public void removeCartItem(CartItem item) {
        cartItems.remove(item);
        item.setCart(null);
    }
}
