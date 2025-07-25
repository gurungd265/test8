package com.example.backend.entity;

public enum OrderStatus {
    PENDING, // 승인대기
    COMPLETED, // 상품 준비중 (주문 승인 완료)
    SHIPPED, // 배송중
    REFUNDED, // 환불
    CANCELLED // 주문 취소
}
