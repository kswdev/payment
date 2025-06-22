package com.example.walletservice.domain;

import lombok.Getter;

@Getter
public class PaymentOrder extends Item {

    private Long sellerId;

    public PaymentOrder(Long id, Long sellerId, Long amount, String orderId) {
        super(amount, orderId, id, ReferenceType.PAYMENT_ORDER);
        this.sellerId = sellerId;
    }
}

