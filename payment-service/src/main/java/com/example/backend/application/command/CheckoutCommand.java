package com.example.backend.application.command;

import java.util.List;

public record CheckoutCommand(
        Long cartId,
        List<Long> productIds,
        Long buyerId,
        String idempotencyKey
) { }
