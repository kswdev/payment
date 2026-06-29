package com.example.backend.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.Map;

@Getter
public class WalletEventMessage {
    private final Type type;
    private Map<String, ?> payload;
    private Map<String, ?> metadata;

    @JsonCreator
    public WalletEventMessage(@JsonProperty("type") Type type) {
        this.type = type;
    }

    @Getter
    public enum Type {
        SUCCESS("정산 처리 성공");

        Type(String description) {
            this.description = description;
        }

        private final String description;
    }

    public String orderId() {
        return payload.get("orderId").toString();
    }
}
