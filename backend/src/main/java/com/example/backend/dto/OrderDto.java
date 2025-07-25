package com.example.backend.dto;

import com.example.backend.entity.OrderStatus;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDto {

    private Long id;
    private Long userId;  // User 엔티티의 PK (혹은 email 등 필요하면 추가 가능)
    private String orderNumber;
    private OrderStatus status;  // enum
    private BigDecimal totalAmount;
    private String paymentMethod;
    private Long shippingAddressId;
    private Long billingAddressId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private List<OrderItemDto> orderItems;

}