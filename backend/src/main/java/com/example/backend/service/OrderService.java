package com.example.backend.service;

import com.example.backend.dto.*;
import com.example.backend.dto.order.OrderRequestDto;
import com.example.backend.dto.order.OrderResponseDto;
import com.example.backend.dto.order.OrderItemDto;
import com.example.backend.dto.user.AddressDto;
import com.example.backend.entity.order.Order;
import com.example.backend.entity.order.OrderItem;
import com.example.backend.entity.order.OrderStatus;
import com.example.backend.entity.payment.Payment;
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

    // 현재 로그인한 유저의 전체 주문 목록 조회
    @Transactional(readOnly = true)
    public List<OrderResponseDto> getOrdersByUserEmail(String userEmail) {
        List<Order> orders = orderRepository.findByUserEmail(userEmail);
        return orders.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // 현재 로그인한 유저의 특정 주문 1건 조회 (상세페이지)
    @Transactional(readOnly = true)
    public OrderResponseDto getOrderByIdAndUserEmail(Long orderId, String userEmail) {
        Order order = orderRepository.findByIdAndUserEmail(orderId, userEmail)
                .orElseThrow(() -> new EntityNotFoundException("注文が存在しないか、アクセス権限がありません。"));

        return convertToDto(order);
    }

    // 주문 상태로 주문 리스트 조회
    @Transactional(readOnly = true)
    public List<OrderResponseDto> getOrdersByStatus(OrderStatus status) {
        List<Order> orders = orderRepository.findByStatus(status);
        return orders.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // 주문번호로 조회
    @Transactional(readOnly = true)
    public OrderResponseDto getOrderByOrderNumber(String orderNumber) {
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

    // 주문 생성 =======================================================================================================

    /**
     * 상세페이지 주문 요청 DTO 기반 주문 생성
     */
    @Transactional
    public OrderResponseDto createOrderFromRequest(String userEmail, OrderRequestDto requestDto) {
        User user = getUserOrThrow(userEmail);
        Order order = buildOrderFromRequest(requestDto, user);
        orderRepository.save(order);
        return convertToDto(order);
    }

    /**
     * 장바구니에 담긴 상품들로부터 주문 생성
     */
    @Transactional
    public OrderResponseDto createOrderFromCart(String userEmail) {
        CartDto cartDto = cartService.getCartByUserEmailOrSessionId(userEmail, null);
        if (cartDto.getItems().isEmpty()) throw new IllegalStateException("カートが空いています。");

        User user = getUserOrThrow(userEmail);
        Order order = buildOrderFromCart(cartDto, user);
        orderRepository.save(order);
        cartService.softClearCart(userEmail, null);
        return convertToDto(order);
    }

    /**
     * 주문 생성 기본 세팅 (공통)
     */
    private Order baseOrderSetup(User user) {
        Order order = new Order();
        order.setUser(user);
        order.setOrderNumber(generateUniqueOrderNumber());
        order.setStatus(OrderStatus.PENDING);
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        return order;
    }

    /**
     * 주문 요청 DTO 기반으로 주문 생성
     */
    private Order buildOrderFromRequest(OrderRequestDto dto, User user) {
        Order order = baseOrderSetup(user);

        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (CartItemDto item : dto.getCartItems()) {
            OrderItem orderItem = toOrderItem(item, order);
            orderItems.add(orderItem);
            total = total.add(orderItem.getSubtotal());
        }

        order.setOrderItems(orderItems);
        order.setTotalAmount(total);

        // 배송지 및 청구지 주소 처리
        if (dto.getDelivery() != null) {
            order.setShippingAddress(addressDtoToEntity(dto.getDelivery().getShippingAddress(), user));
            order.setBillingAddress(addressDtoToEntity(dto.getDelivery().getBillingAddress(), user));
        }

        return order;
    }

    /**
     * 장바구니 DTO 기반으로 주문 생성
     */
    private Order buildOrderFromCart(CartDto cartDto, User user) {
        Order order = baseOrderSetup(user);

        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (CartItemDto item : cartDto.getItems()) {
            OrderItem orderItem = toOrderItem(item, order);
            orderItems.add(orderItem);
            total = total.add(orderItem.getSubtotal());
        }

        order.setOrderItems(orderItems);
        order.setTotalAmount(total);

        return order;
    }

    // 편의 메소드 =======================================================================================================

    /**
     * CartItemDto -> OrderItem 변환
     */
    private OrderItem toOrderItem(CartItemDto item, Order order) {
        OrderItem orderItem = new OrderItem();
        orderItem.setOrder(order);
        orderItem.setProduct(productRepository.findById(item.getProductId())
                .orElseThrow(() -> new EntityNotFoundException("商品が見つかりません。")));
        orderItem.setProductName(item.getProductName());
        orderItem.setProductPrice(item.getPriceAtAddition());
        orderItem.setQuantity(item.getQuantity());
        orderItem.setSubtotal(item.getPriceAtAddition().multiply(BigDecimal.valueOf(item.getQuantity())));
        return orderItem;
    }

    /**
     * AddressDto -> Address Entity 변환
     */
    private Address addressDtoToEntity(AddressDto dto, User user) {
        Address address = new Address();
        address.setUser(user);
        address.setAddressType(dto.getAddressType());
        address.setStreet(dto.getStreet());
        address.setCity(dto.getCity());
        address.setState(dto.getState());
        address.setPostalCode(dto.getPostalCode());
        address.setCountry(dto.getCountry());
        address.setIsDefault(dto.getIsDefault());
        return address;
    }

    /**
     * User email 기반으로 User 조회 및 없으면 예외 발생
     */
    private User getUserOrThrow(String userEmail) {
        return userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("ユーザーが存在しません。"));
    }

    /**
     * 주문 번호 생성 메서드 (현재 시각 + UUID 기반)
     */
    public String generateUniqueOrderNumber() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String randomPart = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return timestamp + "-" + randomPart;
    }

    /**
     * Order Entity -> OrderResponseDto 변환
     */
    private OrderResponseDto convertToDto(Order order) {
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

        return OrderResponseDto.builder()
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

    /**
     * Address Entity -> AddressDto 변환
     */
    private AddressDto addressToDto(Address address) {
        if (address == null) return null;

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

    /**
     * Payment Entity -> PaymentResponseDto 변환
     */
    private PaymentResponseDto paymentToDto(Payment payment) {
        if (payment == null) return null;

        return PaymentResponseDto.builder()
                .id(payment.getId())
                .orderId(payment.getOrder() != null ? payment.getOrder().getId() : null)
                .amount(payment.getAmount())
                .refundAmount(payment.getRefundAmount())
                .paymentMethod(payment.getPaymentMethod().name())
                .transactionId(payment.getTransactionId())
                .status(payment.getStatus().name())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .build();
    }
}
