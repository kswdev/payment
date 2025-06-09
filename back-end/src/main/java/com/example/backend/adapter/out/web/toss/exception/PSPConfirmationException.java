package com.example.backend.adapter.out.web.toss.exception;

import com.example.backend.domain.PaymentStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.List;
import java.util.stream.Stream;

@ToString
@Getter
public class PSPConfirmationException extends RuntimeException {
    private String message;
    private String errorCode;
    private boolean isSuccess;
    private boolean isFailure;
    private boolean isUnknown;
    private boolean isRetryable;
    private Throwable cause;

    @Builder
    public PSPConfirmationException(String message, String errorCode, boolean isSuccess, boolean isFailure, boolean isUnknown, boolean isRetryable, Throwable cause) {
        this.message = message;
        this.errorCode = errorCode;
        this.isSuccess = isSuccess;
        this.isFailure = isFailure;
        this.isUnknown = isUnknown;
        this.isRetryable = isRetryable;
        this.cause = cause;
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
