package com.example.walletservice.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class Item {
    private Long amount;
    private String orderId;
    private Long referencesId;
    private ReferenceType referenceType;

    public enum ReferenceType {
        PAYMENT_ORDER
    }
}
