package com.example.backend.dto.order;

import com.example.backend.dto.CartItemDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderRequestDto { // 클라이언트 -> 서버 요청 DTO. 클라이언트가 입력하는 필드만 포함

    @NotNull
    private OrderCustomerDto customer;

    @NotNull
    private DeliveryRequestDto delivery;

    @NotBlank
    private String paymentMethod;

    @NotEmpty
    private List<@Valid CartItemDto> cartItems;

    @Min(0)
    private int subtotal;

    @Min(0)
    private int shippingFee;

    @Min(0)
    private int tax;

    @Min(0)
    private int totalAmount;
}