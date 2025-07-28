package com.example.backend.entity.order;

public enum OrderStatus {
    PENDING,            // 승인 대기
    COMPLETED,          // 상품 준비중 (주문 완료)
    SHIPPED,            // 배송 중
    REFUNDED,           // 전액 환불
    PARTIALLY_REFUNDED, // 부분 환불
    CANCELLED,          // 주문 취소
    PAYMENT_FAILED      // 결제 실패
}
