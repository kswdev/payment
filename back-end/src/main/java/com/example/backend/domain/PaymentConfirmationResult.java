package com.example.backend.domain;

import lombok.Getter;

@Getter
public class PaymentConfirmationResult {
    private final PaymentStatus status;
    private final PaymentFailure failure;
    private final String message;

    public PaymentConfirmationResult(PaymentStatus status, PaymentFailure failure) {
        this.status = status;
        this.failure = failure;

        if (status == PaymentStatus.FAILURE) {
            if (failure == null) {
                throw new IllegalArgumentException("결제 상태 FAILURE 일 때 PaymentExecutionFailure 는 null 값이 될 수 없습니다.");
            }
        }

        this.message = switch (status) {
            case SUCCESS -> "결제 처리에 성공하였습니다.";
            case FAILURE -> "결제 처리에 실패하였습니다.";
            case UNKNOWN -> "결제 처리 중에 알 수 없는 에러가 발생하였습니다.";
            default -> throw new IllegalStateException("현재 결제 상태 (status: " + status + ") 는 올바르지 않은 상태입니다.");
        };
    }

    // Default constructor with only status
    public PaymentConfirmationResult(PaymentStatus status) {
        this(status, null);
    }
}
