package net.study.ledgerservice.ledger.application.service;

import net.study.ledgerservice.common.UseCase;
import net.study.ledgerservice.ledger.application.port.in.DoubleLedgerEntryRecordUseCase;
import net.study.ledgerservice.ledger.domain.LedgerEventMessage;
import net.study.ledgerservice.ledger.domain.PaymentEventMessage;

@UseCase
public class DoubleLedgerEntryRecordService implements DoubleLedgerEntryRecordUseCase {

    @Override
    public LedgerEventMessage recordDoubleLedgerEntry(PaymentEventMessage message) {

        // TODO: NOT YET IMPLEMENTED
        return null;
    }
}
