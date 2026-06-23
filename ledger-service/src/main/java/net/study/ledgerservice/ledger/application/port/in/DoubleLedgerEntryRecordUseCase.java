package net.study.ledgerservice.ledger.application.port.in;

import net.study.ledgerservice.ledger.domain.PaymentEventMessage;
import org.springframework.messaging.Message;

public interface DoubleLedgerEntryRecordUseCase {

    void recordDoubleLedgerEntry(PaymentEventMessage message);
}
