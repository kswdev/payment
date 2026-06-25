package net.study.ledgerservice.ledger.domain;

import lombok.Getter;

import java.util.Objects;

@Getter
public class DoubleLedgerEntry {
    private LedgerEntry credit;
    private LedgerEntry debit;
    private LedgerTransaction transaction;

    public static DoubleLedgerEntry of(LedgerEntry credit, LedgerEntry debit, LedgerTransaction transaction) {
        if (!Objects.equals(credit.getAmount(), debit.getAmount())) {
            throw new IllegalArgumentException("Credit and debit amount must be same");
        }

        return new DoubleLedgerEntry(credit, debit, transaction);
    }

    private DoubleLedgerEntry(LedgerEntry credit, LedgerEntry debit, LedgerTransaction transaction) {
        this.credit = credit;
        this.debit = debit;
        this.transaction = transaction;
    }
}
