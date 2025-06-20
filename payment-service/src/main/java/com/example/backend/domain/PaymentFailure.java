package com.example.backend.domain;

import lombok.Getter;

@Getter
public class PaymentFailure {
    private String errorCode;
    private String message;

    public PaymentFailure(String errorCode, String message) {
        this.errorCode = errorCode;
        this.message = message;
    }
}