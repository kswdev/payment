package com.example.backend.adaptor.in.web.dto.request;

public record TossPaymentConfirmRequest(
        String paymentKey,
        String orderId,
        String amount
) { }
