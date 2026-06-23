package net.study.ledgerservice.ledger.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

@Getter
@NoArgsConstructor @AllArgsConstructor
public class LedgerEventMessage {

    private Type type;
    private Map<String, ?> payload;
    private Map<String, ?> metadata;

    public String getOrderId() {
        return payload.get("orderId").toString();
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
