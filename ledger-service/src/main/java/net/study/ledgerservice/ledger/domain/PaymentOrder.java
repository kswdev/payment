package net.study.ledgerservice.ledger.domain;

import lombok.Getter;

@Getter
public class PaymentOrder extends Item {

    public PaymentOrder(Long id, Long amount, String orderId) {
        super(id, amount, orderId, ReferenceType.PAYMENT_ORDER);
    }
}
