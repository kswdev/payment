package net.study.ledgerservice.ledger.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LedgerTransaction {
    private ReferenceType referenceType;
    private Long referenceId;
    private String orderId;
}
