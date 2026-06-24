package net.study.ledgerservice.ledger.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PaymentOrder {
    private Long id;
    private Long amount;
    private String orderId;
}
