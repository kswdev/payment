package com.example.backend.adapter.in.web.dto.request;

public record TossPaymentConfirmRequest(
        String paymentKey,
        String orderId,
        Long amount
) { }
