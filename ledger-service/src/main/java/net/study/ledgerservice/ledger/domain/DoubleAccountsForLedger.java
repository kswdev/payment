package net.study.ledgerservice.ledger.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DoubleAccountsForLedger {
    private Account from;
    private Account to;
}
