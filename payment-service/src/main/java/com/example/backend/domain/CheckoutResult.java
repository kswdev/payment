package com.example.backend.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CheckoutResult {
    private final Long amount;
    private final String orderId;
    private final String orderName;
}
