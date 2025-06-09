package com.example.backend.adapter.out.persistence.exception;

import com.example.backend.domain.PaymentStatus;
import lombok.Getter;

@Getter
public class PaymentAlreadyProcessedException extends RuntimeException{

    private final String message;
    private final PaymentStatus paymentStatus;

    public PaymentAlreadyProcessedException(String message, PaymentStatus paymentStatus) {
        this.message = message;
        this.paymentStatus = paymentStatus;
    }
}
