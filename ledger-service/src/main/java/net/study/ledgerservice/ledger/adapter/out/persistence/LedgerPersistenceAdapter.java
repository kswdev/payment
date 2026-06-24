package net.study.ledgerservice.ledger.adapter.out.persistence;

import lombok.RequiredArgsConstructor;
import net.study.ledgerservice.common.PersistenceAdapter;
import net.study.ledgerservice.ledger.adapter.out.persistence.repository.JpaLedgerTransactionRepository;
import net.study.ledgerservice.ledger.application.port.out.DuplicateMessageFilterPort;
import net.study.ledgerservice.ledger.domain.PaymentEventMessage;

@RequiredArgsConstructor
@PersistenceAdapter
public class LedgerPersistenceAdapter implements DuplicateMessageFilterPort {

    private final JpaLedgerTransactionRepository jpaLedgerTransactionRepository;

    @Override
    public Boolean isAlreadyProcess(PaymentEventMessage message) {
        return jpaLedgerTransactionRepository.isExistByOrderId(message.getOrderId());
    }
}
