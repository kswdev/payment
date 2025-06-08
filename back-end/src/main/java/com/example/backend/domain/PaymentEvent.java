package com.example.backend.domain;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
public class PaymentEvent {
    private Long id;
    private Long buyerId;
    private String orderName;
    private String orderId;
    private String paymentKey;
    private PaymentType paymentType;
    private PaymentMethod paymentMethod;
    private LocalDateTime approvedAt;
    private boolean isPaymentDone = false;
    private List<PaymentOrder> paymentOrders;

    @Builder
    public PaymentEvent(Long id, Long buyerId, String orderName, String orderId, String paymentKey, PaymentType paymentType, PaymentMethod paymentMethod, LocalDateTime approvedAt, boolean isPaymentDone, List<PaymentOrder> paymentOrders) {
        this.id = id;
        this.buyerId = buyerId;
        this.orderName = orderName;
        this.orderId = orderId;
        this.paymentKey = paymentKey;
        this.paymentType = paymentType;
        this.paymentMethod = paymentMethod;
        this.approvedAt = approvedAt;
        this.isPaymentDone = isPaymentDone;
        this.paymentOrders = paymentOrders;
    }

    public Long totalAmount() {
        return this.paymentOrders.stream()
                .map(PaymentOrder::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .longValue();
    }
}
