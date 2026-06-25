package net.study.ledgerservice.ledger.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Item {
    private Long id;
    private Long amount;
    private String orderId;
    private ReferenceType referenceType;
}
