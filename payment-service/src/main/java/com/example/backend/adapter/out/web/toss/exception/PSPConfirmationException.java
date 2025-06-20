package com.example.backend.adapter.out.web.toss.exception;

import com.example.backend.domain.PaymentStatus;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.stream.Stream;

@Getter
public class PSPConfirmationException extends RuntimeException {
    private final String message;
    private final String errorCode;
    private final boolean isSuccess;
    private final boolean isFailure;
    private final boolean isUnknown;
    private final boolean isRetryable;

    @Builder
    public PSPConfirmationException(String message, String errorCode, boolean isSuccess, boolean isFailure, boolean isUnknown, boolean isRetryable, Throwable cause) {
        this.message = message;
        this.errorCode = errorCode;
        this.isSuccess = isSuccess;
        this.isFailure = isFailure;
        this.isUnknown = isUnknown;
        this.isRetryable = isRetryable;
        init();
    }

    public PaymentStatus paymentStatus() {
        if (isSuccess) return PaymentStatus.SUCCESS;
        else if (isFailure) return PaymentStatus.FAILURE;
        else return PaymentStatus.UNKNOWN;
    }

    private void init() {
        assert (Stream.of(isFailure, isSuccess, isUnknown).filter(b -> b).count() == 1)
                : String.format("Only one of [isFailure, isSuccess, isUnknown] can be true. But %s is true.", List.of(isFailure, isSuccess, isUnknown));

    }
}
