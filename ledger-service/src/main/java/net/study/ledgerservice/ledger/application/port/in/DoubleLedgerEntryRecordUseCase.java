package net.study.ledgerservice.ledger.application.port.in;

import net.study.ledgerservice.ledger.domain.LedgerEventMessage;
import net.study.ledgerservice.ledger.domain.PaymentEventMessage;

public interface DoubleLedgerEntryRecordUseCase {

    LedgerEventMessage recordDoubleLedgerEntry(PaymentEventMessage message);
}
