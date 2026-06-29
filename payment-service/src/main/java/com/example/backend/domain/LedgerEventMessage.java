package com.example.backend.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.Map;

@Getter
public class LedgerEventMessage {
    private final Type type;
    private Map<String, ?> payload;
    private Map<String, ?> metadata;

    @JsonCreator
    public LedgerEventMessage(@JsonProperty("type") Type type) {
        this.type = type;
    }

    @Getter
    public enum Type {
        SUCCESS("장부 기입 성공");

        Type(String description) {
            this.description = description;
        }

        private final String description;
    }
}
