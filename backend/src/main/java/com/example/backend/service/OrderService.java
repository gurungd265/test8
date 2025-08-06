package com.example.backend.service;

import com.example.backend.dto.CartDto;
import com.example.backend.dto.CartItemDto;
import com.example.backend.dto.PaymentResponseDto;
import com.example.backend.dto.order.DeliveryRequestDto;
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
import com.example.backend.repository.AddressRepository;
import com.example.backend.repository.CartRepository;
import com.example.backend.repository.OrderRepository;
import com.example.backend.repository.OrderItemRepository;
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
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final CartService cartService;
    private final OrderItemRepository orderItemRepository;
    private final PaymentService paymentService;
    private final AddressRepository addressRepository;

    @Transactional(readOnly = true)
    public List<OrderResponseDto> getOrdersByUserEmail(String userEmail) {
        List<Order> orders = orderRepository.findByUserEmail(userEmail);
        return orders.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public OrderResponseDto getOrderByIdAndUserEmail(Long orderId, String userEmail) {
        Order order = orderRepository.findByIdAndUserEmail(orderId, userEmail)
                .orElseThrow(() -> new EntityNotFoundException("注文が存在しないか、アクセス権限がありません。"));
        return convertToDto(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponseDto> getOrdersByStatus(OrderStatus status) {
        List<Order> orders = orderRepository.findByStatus(status);
        return orders.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public OrderResponseDto getOrderByOrderNumber(String orderNumber) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new EntityNotFoundException("注文番号が見つかりません: " + orderNumber));
        return convertToDto(order);
    }

    @Transactional(readOnly = true)
    public List<PaymentResponseDto> getPaymentsByOrderId(Long orderId, String userEmail) {
        return paymentService.getPaymentsByOrderId(orderId, userEmail);
    }

    @Transactional
    public void cancelOrder(Long orderId, String userEmail) {
        Order order = orderRepository.findByIdAndUserEmail(orderId, userEmail)
                .orElseThrow(() -> new EntityNotFoundException("注文が存在しないか、アクセス権限がありません。"));

        if (order.getStatus() == OrderStatus.SHIPPED || order.getStatus() == OrderStatus.COMPLETED) {
            throw new IllegalStateException("この注文はキャンセルできません。");
        }

        order.setStatus(OrderStatus.CANCELLED);

        for (OrderItem item : order.getOrderItems()) {
            productRepository.findById(item.getProduct().getId())
                    .ifPresent(product -> {
                        product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
                    });
        }
        orderRepository.save(order);
    }

    private Order baseOrderSetup(User user) {
        Order order = new Order();
        order.setUser(user);
        order.setOrderNumber(generateUniqueOrderNumber());
        order.setStatus(OrderStatus.PENDING);
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        return order;
    }

    public String generateUniqueOrderNumber() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String randomPart = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return timestamp + "-" + randomPart;
    }

    private User getUserOrThrow(String userEmail) {
        return userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("ユーザーが存在しません。"));
    }

    private BigDecimal calculateShippingFee() {
        return BigDecimal.valueOf(600);
    }

    @Transactional
    public OrderResponseDto createOrderFromRequest(String userEmail, OrderRequestDto requestDto) {
        User user = getUserOrThrow(userEmail);
        Order order = buildOrderFromRequest(requestDto, user);
        orderRepository.save(order);
        return convertToDto(order);
    }

    private Order buildOrderFromRequest(OrderRequestDto dto, User user) {
        Order order = baseOrderSetup(user);
        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;

        if (dto.getCartItems() != null && !dto.getCartItems().isEmpty()) {
            for (CartItemDto item : dto.getCartItems()) {
                OrderItem orderItem = toOrderItem(item, order);
                orderItems.add(orderItem);
                // [수정] priceAtAddition이 null일 경우를 대비하여 Optional.ofNullable 사용
                BigDecimal itemPrice = Optional.ofNullable(item.getPriceAtAddition()).orElse(BigDecimal.ZERO);
                subtotal = subtotal.add(itemPrice.multiply(BigDecimal.valueOf(item.getQuantity())));
            }
        } else {
            subtotal = BigDecimal.valueOf(Optional.ofNullable(dto.getSubtotal()).orElse(0));
        }

        BigDecimal shippingFee = calculateShippingFee();
        BigDecimal tax = subtotal.multiply(BigDecimal.valueOf(0.1)).setScale(0, BigDecimal.ROUND_FLOOR);
        BigDecimal totalAmount = subtotal.add(shippingFee).add(tax);

        order.setOrderItems(orderItems);
        order.setSubtotal(subtotal);
        order.setShippingFee(shippingFee);
        order.setTax(tax);
        order.setTotalAmount(totalAmount);

        if (dto.getShippingAddressId() != null) {
            Address shippingAddress = addressRepository.findById(dto.getShippingAddressId())
                    .orElseThrow(() -> new EntityNotFoundException("配送先住所が見つかりません。ID: " + dto.getShippingAddressId()));
            order.setShippingAddress(shippingAddress);
        }

        if (dto.getBillingAddressId() != null) {
            Address billingAddress = addressRepository.findById(dto.getBillingAddressId())
                    .orElseThrow(() -> new EntityNotFoundException("請求先住所が見つかりません。ID: " + dto.getBillingAddressId()));
            order.setBillingAddress(billingAddress);
        }
        return order;
    }

    @Transactional
    public OrderResponseDto createOrderFromCart(String userEmail, OrderRequestDto requestDto) {
        CartDto cartDto = cartService.getCartByUserEmailOrSessionId(userEmail, null);
        if (cartDto.getItems().isEmpty()) throw new IllegalStateException("カートが空いています。");

        User user = getUserOrThrow(userEmail);
        Order order = buildOrderFromCart(cartDto, user);

        order.setPaymentMethod(requestDto.getPaymentMethod());

        Address shippingAddress = addressRepository.findById(requestDto.getShippingAddressId())
                .orElseThrow(() -> new EntityNotFoundException("配送先住所が見つかりません。"));
        order.setShippingAddress(shippingAddress);

        Address billingAddress = addressRepository.findById(requestDto.getBillingAddressId())
                .orElseThrow(() -> new EntityNotFoundException("請求先住所が見つかりません。"));
        order.setBillingAddress(billingAddress);

        orderRepository.save(order);

        String transactionId = UUID.randomUUID().toString();
        Payment createdPayment = paymentService.createPayment(
                user.getEmail(),
                order.getId(),
                order.getTotalAmount(),
                order.getPaymentMethod(),
                transactionId
        );

        order.getPayments().add(createdPayment);
        cartService.softClearCart(userEmail, null);

        return convertToDto(order);
    }

    private Order buildOrderFromCart(CartDto cartDto, User user) {
        Order order = baseOrderSetup(user);
        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;

        for (CartItemDto item : cartDto.getItems()) {
            OrderItem orderItem = toOrderItem(item, order);
            orderItems.add(orderItem);
            // [수정] priceAtAddition이 null일 경우를 대비하여 Optional.ofNullable 사용
            BigDecimal itemPrice = Optional.ofNullable(item.getPriceAtAddition()).orElse(BigDecimal.ZERO);
            subtotal = subtotal.add(itemPrice.multiply(BigDecimal.valueOf(item.getQuantity())));
        }

        BigDecimal shippingFee = calculateShippingFee();
        BigDecimal tax = subtotal.multiply(BigDecimal.valueOf(0.1)).setScale(0, BigDecimal.ROUND_FLOOR);
        BigDecimal totalAmount = subtotal.add(shippingFee).add(tax);

        order.setOrderItems(orderItems);
        order.setSubtotal(subtotal);
        order.setShippingFee(shippingFee);
        order.setTax(tax);
        order.setTotalAmount(totalAmount);
        return order;
    }

    private OrderItem toOrderItem(CartItemDto item, Order order) {
        OrderItem orderItem = new OrderItem();
        orderItem.setOrder(order);
        orderItem.setProduct(productRepository.findById(item.getProductId())
                .orElseThrow(() -> new EntityNotFoundException("商品が見つかりません。")));
        orderItem.setProductName(item.getProductName());
        // [핵심 수정] priceAtAddition이 null일 경우 BigDecimal.ZERO로 대체
        BigDecimal priceAtAddition = Optional.ofNullable(item.getPriceAtAddition()).orElse(BigDecimal.ZERO);
        orderItem.setProductPrice(priceAtAddition);
        orderItem.setQuantity(item.getQuantity());
        orderItem.setSubtotal(priceAtAddition.multiply(BigDecimal.valueOf(item.getQuantity()))); // 수정된 priceAtAddition 사용
        return orderItem;
    }

    private OrderResponseDto convertToDto(Order order) {
        List<OrderItemDto> orderItemDtos = order.getOrderItems().stream()
                .filter(item -> item.getOrder() != null && item.getOrder().getId().equals(order.getId()))
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
                .filter(payment -> payment.getOrder() != null && payment.getOrder().getId().equals(order.getId()))
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