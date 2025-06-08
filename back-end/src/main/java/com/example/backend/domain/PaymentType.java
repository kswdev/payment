package com.example.backend.domain;

import lombok.Getter;

public enum PaymentType {
    NORMAL("일반 결제");

    PaymentType(String description) {
        this.description = description;
    }

    @Getter private final String description;
}
