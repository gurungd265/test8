package com.example.backend.entity.payment;

public enum PaymentStatus {
    INITIATED,          // 결제 시도 시작
    PENDING,            // 결제 승인 대기 중
    COMPLETED,          // 결제 완료
    FAILED,             // 결제 실패
    CANCELED,          // 사용자가 결제 취소
    EXPIRED,            // 결제 시도 만료
    REFUNDED,           // 전액 환불
    PARTIALLY_REFUNDED, // 부분 환불
    CHARGEBACK          // 카드사 분쟁 등으로 인한 회수
}
