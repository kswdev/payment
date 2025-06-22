package com.example.walletservice.domain;

import lombok.Getter;

import java.util.Map;

public class WalletEventMessage {

    private Type type;
    private Map<String, ?> payload;
    private Map<String, ?> metadata;

    public WalletEventMessage(Type type, Map<String, ?> payload) {
        this.type = type;
        this.payload = payload;
    }

    @Getter
    public enum Type {
        SUCCESS("정산 성공");

        Type(String description) {
            this.description = description;
        }

        private final String description;
    }
}
