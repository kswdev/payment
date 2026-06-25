package net.study.ledgerservice.ledger.application.port.out;

import net.study.ledgerservice.ledger.domain.DoubleLedgerEntry;

import java.util.List;

public interface SaveDoubleLedgerEntryPort {

    List<DoubleLedgerEntry> saveDoubleLedgerEntries(List<DoubleLedgerEntry> doubleLedgerEntries);
}
