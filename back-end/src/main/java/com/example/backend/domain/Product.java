package com.example.backend.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class Product {
    private final Long id;
    private final BigDecimal amount;
    private final int quantity;
    private final String name;
    private final Long sellerId;
}
