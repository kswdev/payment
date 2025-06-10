package com.example.backend.domain;

import lombok.Builder;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class PendingPaymentEvent  {
    private final Long paymentEventId;
    private final String orderId;
    private final String paymentKey;
    private final List<PendingPaymentOrder> pendingPaymentOrders = new ArrayList<>();

    @Builder
    public PendingPaymentEvent(Long paymentEventId, String orderId, String paymentKey, List<PendingPaymentOrder> pendingPaymentOrders) {
        this.paymentEventId = paymentEventId;
        this.orderId = orderId;
        this.paymentKey = paymentKey;
        this.pendingPaymentOrders.addAll(pendingPaymentOrders);
    }

    public Long getTotalAmount() {
        return pendingPaymentOrders.stream()
                .mapToLong(PendingPaymentOrder::getAmount)
                .sum();
    }
}
