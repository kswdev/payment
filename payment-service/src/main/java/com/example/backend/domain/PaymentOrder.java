package com.example.backend.domain;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class PaymentOrder {
    private Long id;
    private Long paymentEventId;
    private Long sellerId;
    private Long buyerId;
    private Long productId;
    private String orderId;
    private BigDecimal amount;
    private PaymentStatus paymentStatus;
    private boolean isLedgerUpdated = false;
    private boolean isWalletUpdated = false;
    private boolean isPaymentDone = false;

    @Builder
    public PaymentOrder(Long id, Long paymentEventId, Long sellerId, Long buyerId, Long productId, String orderId, BigDecimal amount, PaymentStatus paymentStatus, boolean isLedgerUpdated, boolean isWalletUpdated, boolean isPaymentDone) {
        this.id = id;
        this.paymentEventId = paymentEventId;
        this.sellerId = sellerId;
        this.buyerId = buyerId;
        this.productId = productId;
        this.orderId = orderId;
        this.amount = amount;
        this.paymentStatus = paymentStatus;
        this.isLedgerUpdated = isLedgerUpdated;
        this.isWalletUpdated = isWalletUpdated;
        this.isPaymentDone = isPaymentDone;
    }
}
