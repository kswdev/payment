package com.example.backend.application.command;

public record PaymentConfirmCommand(
        String paymentKey,
        String orderId,
        Long amount
) {
}
