package com.example.backend.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
public class PendingPaymentOrder {
    private final Long paymentOrderId;
    private final PaymentStatus paymentStatus;
    private final Long amount;
    private final int failedCount;
    private final int threshold;

    @Builder
    public PendingPaymentOrder(Long paymentOrderId, PaymentStatus paymentStatus, Long amount, int failedCount, int threshold) {
        this.paymentOrderId = paymentOrderId;
        this.paymentStatus = paymentStatus;
        this.amount = amount;
        this.failedCount = failedCount;
        this.threshold = threshold;
    }
}
