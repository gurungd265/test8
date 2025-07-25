package com.example.backend.controller;

import com.example.backend.dto.OrderDto;
import com.example.backend.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /**
     * ADMIN, USER & ANONYMOUS (non-login) accessible
     *
     * 주문 목록 조회	    GET	    /api/orders
     * 주문 상세 조회	    GET	    /api/orders/{orderId}
     * 주문 취소	        POST    /PUT/DELETE	/api/orders/{orderId}/cancel
     */

    // 주문 목록 조회 (로그인 유저 기준)
    @GetMapping
    public ResponseEntity<List<OrderDto>> getUserOrders(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }
        String userEmail = principal.getName();
        List<OrderDto> orders = orderService.getOrdersByUserEmail(userEmail);
        return ResponseEntity.ok(orders);
    }

    // 주문 상세 조회
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDto> getOrder(@PathVariable Long orderId, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).build(); // 인증 실패
        }
        String userEmail = principal.getName();
        OrderDto orderDto = orderService.getOrderByIdAndUserEmail(orderId, userEmail);
        return ResponseEntity.ok(orderDto);
    }

    // 주문 취소
    @DeleteMapping("/{orderId}/cancel")
    public ResponseEntity<Void> cancelOrder(@PathVariable Long orderId, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }
        String userEmail = principal.getName();
        orderService.cancelOrder(orderId, userEmail);
        return ResponseEntity.noContent().build();
    }
}
