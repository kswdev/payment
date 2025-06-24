package com.example.walletservice.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

@Getter
@NoArgsConstructor @AllArgsConstructor
public class PaymentEventMessage  {

    private Type type;
    private Map<String, ?> payload;
    private Map<String, ?> metadata;

    public String getOrderId() {
        return payload.get("orderId").toString();
    }

    @Getter
    public enum Type {
        PAYMENT_CONFIRMATION_SUCCESS("결제 승인 완료");

        Type(String description) {
            this.description = description;
        }

        private final String description;
    }
}
