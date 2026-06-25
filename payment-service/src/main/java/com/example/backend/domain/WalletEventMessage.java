package com.example.backend.domain;

import lombok.Getter;

import java.util.Map;

@Getter
public class WalletEventMessage {
    private final PaymentEventMessage.Type type;
    private Map<String, ?> payload;
    private Map<String, ?> metadata;

    public WalletEventMessage(PaymentEventMessage.Type type) {
        this.type = type;
    }
}
