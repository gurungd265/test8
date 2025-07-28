package com.example.backend.service;

import com.example.backend.dto.*;
import com.example.backend.dto.user.AddressDto;
import com.example.backend.entity.Order;
import com.example.backend.entity.OrderItem;
import com.example.backend.entity.OrderStatus;
import com.example.backend.entity.Payment;
import com.example.backend.entity.user.Address;
import com.example.backend.entity.user.User;
import com.example.backend.repository.OrderRepository;
import com.example.backend.repository.PaymentRepository;
import com.example.backend.repository.ProductRepository;
import com.example.backend.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;
    private final CartService cartService;

    // 주문 생성 관리
    @Transactional
    public OrderDto createOrderFromCart(String userEmail) {
        // 1. 유저 이메일로 카트 조회 (비회원 주문 불가)
        CartDto cartDto = cartService.getCartByUserEmailOrSessionId(userEmail, null);

        if (cartDto.getItems().isEmpty()) {
            throw new IllegalStateException("カートが空いています。");
        }

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("ユーザーが存在しません。"));

        // 2. 주문 빌드 및 재고 체크/차감
        Order order = buildOrderFromCart(cartDto, user);

        // 3. 주문 저장
        orderRepository.save(order);

        // 4. 주문 완료 후 카트 비우기 (soft delete)
        cartService.softClearCart(userEmail, null);

        // 5. 주문 DTO 변환 및 반환
        return convertToDto(order);
    }

    // 장바구니로 주문 생성
    private Order buildOrderFromCart(CartDto cartDto, User user) {
        Order order = new Order();
        order.setUser(user);
        order.setOrderNumber(generateUniqueOrderNumber());
        order.setStatus(OrderStatus.PENDING);
        order.setTotalAmount(BigDecimal.ZERO);
        order.setOrderItems(new ArrayList<>());

        BigDecimal totalAmount = BigDecimal.ZERO;
        for (CartItemDto cartItemDto : cartDto.getItems()) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(productRepository.findById(cartItemDto.getProductId())
                    .orElseThrow(() -> new EntityNotFoundException("商品が見つかりません。")));
            orderItem.setProductName(cartItemDto.getProductName());
            orderItem.setProductPrice(cartItemDto.getPriceAtAddition());
            orderItem.setQuantity(cartItemDto.getQuantity());
            orderItem.setSubtotal(cartItemDto.getPriceAtAddition().multiply(BigDecimal.valueOf(cartItemDto.getQuantity())));

            order.addOrderItem(orderItem);

            totalAmount = totalAmount.add(orderItem.getSubtotal());
        }

        order.setTotalAmount(totalAmount);
        return order;
    }

    // 현재 로그인한 유저의 전체 주문 목록 조회
    @Transactional(readOnly = true)
    public List<OrderDto> getOrdersByUserEmail(String userEmail) {
        List<Order> orders = orderRepository.findByUserEmail(userEmail);
        return orders.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // 현재 로그인한 유저의 특정 주문 1건 조회 (상세페이지)
    @Transactional(readOnly = true)
    public OrderDto getOrderByIdAndUserEmail(Long orderId, String userEmail) {
        Order order = orderRepository.findByIdAndUserEmail(orderId, userEmail)
                .orElseThrow(() -> new EntityNotFoundException("注文が存在しないか、アクセス権限がありません。"));

        return convertToDto(order);
    }

    // 주문 상태로 주문 리스트 조회
    @Transactional(readOnly = true)
    public List<OrderDto> getOrdersByStatus(OrderStatus status) {
        List<Order> orders = orderRepository.findByStatus(status);
        return orders.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // 주문번호로 조회
    @Transactional(readOnly = true)
    public OrderDto getOrderByOrderNumber(String orderNumber) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new EntityNotFoundException("注文番号が見つかりません: " + orderNumber));
        return convertToDto(order);
    }

    // 주문번호로 결제내역 조회
    @Transactional(readOnly = true)
    public List<PaymentResponseDto> getPaymentsByOrderId(Long orderId, String userEmail) {
        // 1. 주문 존재 및 사용자 권한 확인
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found"));

        if (!order.getUser().getEmail().equals(userEmail)) {
            throw new AccessDeniedException("No permission to access this order's payments.");
        }

        // 2. 결제 내역 조회
        List<Payment> payments = paymentRepository.findByOrder(order);

        // 3. DTO 변환 및 반환
        return payments.stream()
                .map(this::paymentToDto)
                .collect(Collectors.toList());
    }

    // 주문 취소
    @Transactional
    public void cancelOrder(Long orderId, String userEmail) {
        Order order = orderRepository.findByIdAndUserEmail(orderId, userEmail)
                .orElseThrow(() -> new EntityNotFoundException("注文が存在しないか、アクセス権限がありません。"));

        // 이미 배송 중이거나 완료된 주문은 취소 불가
        if (order.getStatus() == OrderStatus.SHIPPED || order.getStatus() == OrderStatus.COMPLETED) {
            throw new IllegalStateException("この注文はキャンセルできません。");
        }

        // 주문 상태 변경
        order.setStatus(OrderStatus.CANCELLED);

        // 재고 복구 로직 (상품 재고 다시 증가시키기)
        for (OrderItem item : order.getOrderItems()) {
            productRepository.findById(item.getProduct().getId())
                    .ifPresent(product -> {
                        product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
                    });
        }

        // 변경사항 저장
        orderRepository.save(order);
    }

    // 편의 메소드 ======================================================================================================

    // OrderNumber 생성 메소드
    public String generateUniqueOrderNumber() {
        // 현재 시간 기준 YYYYMMDDHHMMSS 형식
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

        // 랜덤 UUID 앞 8자리 (충돌 확률 낮춤)
        String randomPart = UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        // 예: 20250725153045-1A2B3C4D
        return timestamp + "-" + randomPart;
    }

    // 주문 정보 Entity -> DTO
    private OrderDto convertToDto(Order order) {
        List<OrderItemDto> orderItemDtos = order.getOrderItems().stream()
                .map(item -> new OrderItemDto(
                        item.getId(),
                        item.getProduct().getId(),
                        item.getProductName(),
                        item.getProductPrice(),
                        item.getQuantity(),
                        item.getSubtotal()
                ))
                .toList();

        List<PaymentResponseDto> paymentResponseDtos = order.getPayments().stream()
                .map(this::paymentToDto)
                .toList();

        return OrderDto.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .shippingAddress(order.getShippingAddress() != null ? addressToDto(order.getShippingAddress()) : null)
                .billingAddress(order.getBillingAddress() != null ? addressToDto(order.getBillingAddress()) : null)
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .orderItems(orderItemDtos)
                .payments(paymentResponseDtos)
                .build();
    }

    // 주소 Entity -> Dto
    private AddressDto addressToDto(Address address) {
        if (address == null) {
            return null;
        }
        return AddressDto.builder()
                .id(address.getId())
                .addressType(address.getAddressType())
                .street(address.getStreet())
                .city(address.getCity())
                .state(address.getState())
                .postalCode(address.getPostalCode())
                .country(address.getCountry())
                .isDefault(address.getIsDefault())
                .build();
    }

    // 결제 정보 Entity -> Dto
    private PaymentResponseDto paymentToDto(Payment payment) {
        if (payment == null) return null;

        return PaymentResponseDto.builder()
                .id(payment.getId())
                .orderId(payment.getOrder() != null ? payment.getOrder().getId() : null)
                .amount(payment.getAmount())
                .refundAmount(payment.getRefundAmount())
                .paymentMethod(payment.getPaymentMethod())
                .transactionId(payment.getTransactionId())
                .status(payment.getStatus().name())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .build();
    }
}
