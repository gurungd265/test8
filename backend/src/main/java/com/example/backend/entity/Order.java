package com.example.backend.entity;

import com.example.backend.entity.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.Where;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(
        name = "orders",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_order_order_number",
                        columnNames = "order_number"
                )
        }
)
@Where(clause = "deleted_at IS NULL")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "order_number", unique = true, nullable = false)
    private String orderNumber;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @Column(name = "total_amount")
    private BigDecimal totalAmount;

    @Column(name = "payment_method")
    private String paymentMethod;

    @Column(name = "shipping_address_id")
    private Long shippingAddressId;

    @Column(name = "billing_address_id")
    private Long billingAddressId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = true, updatable = true)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at", nullable = true, updatable = true)
    private LocalDateTime deletedAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems;

    public void addOrderItem(OrderItem item) {
        if (orderItems == null) {
            orderItems = new ArrayList<>();
        }
        orderItems.add(item);
        item.setOrder(this);
    }

    public void removeOrderItem(OrderItem item) {
        if (orderItems != null) {
            orderItems.remove(item);
        }
        item.setOrder(null);
    }
}