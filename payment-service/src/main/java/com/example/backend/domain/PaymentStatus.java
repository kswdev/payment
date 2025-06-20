package com.example.backend.domain;

import lombok.Getter;

import java.util.Arrays;

public enum PaymentStatus {
    NOT_STARTED("결제 승인 전"),
    EXECUTING("결제 승인 중"),
    SUCCESS("결제 승인 성공"),
    FAILURE("결제 승인 실패"),
    UNKNOWN("결제 승인 알 수 없는 상태");

    PaymentStatus(String description) {
        this.description = description;
    }

    @Getter private final String description;

    public static PaymentStatus get(String status) {
        return Arrays.stream(PaymentStatus.values())
                .filter(paymentMethod -> paymentMethod.name().equals(status))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        String.format("Payment Method (method: %s) 는 올바르지 않은 결제 방법입니다.", status)
                ));
    }
}
