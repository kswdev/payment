package com.example.backend.domain;

import lombok.Getter;

import java.util.Arrays;

public enum PaymentMethod {
    EASY_PAY("간편결제");

    PaymentMethod(String description) {
        this.description = description;
    }

    @Getter private final String description;

    public static PaymentMethod get(String method) {
        return Arrays.stream(PaymentMethod.values())
                .filter(paymentMethod -> paymentMethod.getDescription().equals(method))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        String.format("Payment Method (method: %s) 는 올바르지 않은 결제 방법입니다.", method)
                ));
    }

}
