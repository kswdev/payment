package com.example.backend.domain;

import lombok.Getter;

import java.util.Map;

@Getter
public class PaymentEventMessage {

    private final Type type;
    private Map<String, ?> payload;
    private Map<String, ?> metadata;

    public PaymentEventMessage(Type type) {
        this.type = type;
    }

    public PaymentEventMessage(Type type, Map<String, ?> payload, Map<String, ?> metadata) {
        this.type = type;
        this.payload = payload;
        this.metadata = metadata;
    }

    public enum Type {
        PAYMENT_CONFIRMATION_SUCCESS("결제 승인 완료 이벤트");

        Type(String description) {
            this.description = description;
        }

        private final String description;

        public String getDescription() {
            return description;
        }
    }
}