package com.example.backend.adapter.out.persistence.exception;

public class PaymentValidationException extends RuntimeException {

    private final String message;

    public PaymentValidationException(String message) {
        this.message = message;
    }
}
