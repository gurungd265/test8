package com.example.backend.controller;

import com.example.backend.dto.OrderDto;
import com.example.backend.dto.PaymentResponseDto;
import com.example.backend.entity.order.OrderStatus;
import com.example.backend.service.OrderService;
import com.example.backend.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;
    private final PaymentService paymentService;

    /**
     * 주문 목록 조회            GET	    /api/orders
     * 주문 상세 조회            GET	    /api/orders/{orderId}
     * 주문 취소                DELETE	/api/orders/{orderId}/cancel
     * 주문별 결제내역 조회       GET      /api/orders/{orderId}/payments
     * 주문상태별 목록 조회       GET      /api/status/{status}
     */

    // 주문 생성
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<OrderDto> createOrder(@AuthenticationPrincipal UserDetails userDetails) {
        log.info("Create order request by user: {}", userDetails != null ? userDetails.getUsername() : "anonymous");
        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }
        String userEmail = userDetails.getUsername();
        OrderDto orderDto = orderService.createOrderFromCart(userEmail);
        log.info("Order created successfully: orderId={}, user={}", orderDto.getId(), userEmail);
        return ResponseEntity.status(201).body(orderDto);
    }

    // 주문 목록 조회 (로그인 유저 기준)
    @GetMapping
    public ResponseEntity<List<OrderDto>> getUserOrders(Principal principal) {
        log.info("Get orders request by user: {}", principal != null ? principal.getName() : "anonymous");
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }
        String userEmail = principal.getName();
        List<OrderDto> orders = orderService.getOrdersByUserEmail(userEmail);
        log.info("Orders retrieved: count={}, user={}", orders.size(), userEmail);
        return ResponseEntity.ok(orders);
    }

    // 주문 상세 조회
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDto> getOrder(@PathVariable Long orderId, Principal principal) {
        log.info("Get order detail request: orderId={}, user={}", orderId, principal != null ? principal.getName() : "anonymous");
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }
        String userEmail = principal.getName();
        OrderDto orderDto = orderService.getOrderByIdAndUserEmail(orderId, userEmail);
        log.info("Order detail retrieved: orderId={}, user={}", orderDto.getId(), userEmail);
        return ResponseEntity.ok(orderDto);
    }

    // 주문 취소
    @DeleteMapping("/{orderId}/cancel")
    public ResponseEntity<Void> cancelOrder(@PathVariable Long orderId, Principal principal) {
        log.info("Cancel order request: orderId={}, user={}", orderId, principal != null ? principal.getName() : "anonymous");
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }
        String userEmail = principal.getName();
        orderService.cancelOrder(orderId, userEmail);
        log.info("Order cancelled successfully: orderId={}, user={}", orderId, userEmail);
        return ResponseEntity.noContent().build();
    }

    // 주문별 결제 내역 조회
    @GetMapping("/{orderId}/payments")
    public ResponseEntity<List<PaymentResponseDto>> getPaymentsByOrderId(
            @PathVariable Long orderId, Principal principal) {
        log.info("Get payments by order request: orderId={}, user={}", orderId, principal != null ? principal.getName() : "anonymous");
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }
        String userEmail = principal.getName();
        List<PaymentResponseDto> payments = paymentService.getPaymentsByOrderId(orderId, userEmail);
        log.info("Payments retrieved: count={}, orderId={}, user={}", payments.size(), orderId, userEmail);
        return ResponseEntity.ok(payments);
    }

    // 주문 상태별 목록 조회 (관리자 권한)
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<OrderDto>> getOrdersByStatus(
            @PathVariable OrderStatus status
    ) {
        log.info("Get orders by status request: status={}", status);
        List<OrderDto> orders = orderService.getOrdersByStatus(status);
        log.info("Orders retrieved by status: count={}, status={}", orders.size(), status);
        return ResponseEntity.ok(orders);
    }

}
