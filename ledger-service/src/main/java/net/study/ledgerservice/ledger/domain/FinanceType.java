package net.study.ledgerservice.ledger.domain;

import lombok.Getter;

@Getter
public enum FinanceType {
    PAYMENT_ORDER("결제 주가");

    FinanceType(String description) {
        this.description = description;
    }

    private final String description;
}
