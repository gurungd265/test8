package com.example.backend.dto.order;

import com.example.backend.dto.user.AddressDto;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryRequestDto {

    @NotEmpty(message = "配送日は必須項目です。")
    private String date;

    @NotEmpty(message = "配送時間は必須項目です。")
    private String time;

    private AddressDto shippingAddress;
    private AddressDto billingAddress;
}