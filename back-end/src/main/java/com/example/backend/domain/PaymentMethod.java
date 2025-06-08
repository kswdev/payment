package com.example.backend.domain;

import lombok.Getter;

public enum PaymentMethod {
    EASY_PAY("간편 결제");

    PaymentMethod(String description) {
        this.description = description;
    }

    @Getter private final String description;
}
