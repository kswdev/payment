package net.study.ledgerservice.ledger.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LedgerEntry {
    private Account account;
    private Long amount;
    private LedgerEntryType type;

    public enum LedgerEntryType {
        CREDIT, DEBIT
    }
}
