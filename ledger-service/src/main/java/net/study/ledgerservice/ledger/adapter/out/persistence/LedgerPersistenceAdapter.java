package net.study.ledgerservice.ledger.adapter.out.persistence;

import lombok.RequiredArgsConstructor;
import net.study.ledgerservice.common.IdempotencyCreator;
import net.study.ledgerservice.common.PersistenceAdapter;
import net.study.ledgerservice.ledger.adapter.out.persistence.entity.LedgerEntryEntity;
import net.study.ledgerservice.ledger.adapter.out.persistence.entity.LedgerTransactionEntity;
import net.study.ledgerservice.ledger.adapter.out.persistence.repository.JpaLedgerEntryRepository;
import net.study.ledgerservice.ledger.adapter.out.persistence.repository.JpaLedgerTransactionRepository;
import net.study.ledgerservice.ledger.application.port.out.DuplicateMessageFilterPort;
import net.study.ledgerservice.ledger.application.port.out.SaveDoubleLedgerEntryPort;
import net.study.ledgerservice.ledger.domain.DoubleLedgerEntry;
import net.study.ledgerservice.ledger.domain.PaymentEventMessage;

import java.math.BigDecimal;
import java.util.List;

@RequiredArgsConstructor
@PersistenceAdapter
public class LedgerPersistenceAdapter implements DuplicateMessageFilterPort, SaveDoubleLedgerEntryPort {

    private final JpaLedgerTransactionRepository jpaLedgerTransactionRepository;
    private final JpaLedgerEntryRepository jpaLedgerEntryRepository;

    @Override
    public Boolean isAlreadyProcess(PaymentEventMessage message) {
        return jpaLedgerTransactionRepository.isExistByOrderId(message.getOrderId());
    }

    @Override
    public List<DoubleLedgerEntry>  saveDoubleLedgerEntries(List<DoubleLedgerEntry> doubleLedgerEntries) {
        List<LedgerEntryEntity> ledgerEntryEntities = doubleLedgerEntries.stream()
                .flatMap(doubleLedgerEntry -> createLedgerEntryEntity(doubleLedgerEntry).stream())
                .toList();

        jpaLedgerEntryRepository.saveAll(ledgerEntryEntities);

        return doubleLedgerEntries;
    }

    private List<LedgerEntryEntity> createLedgerEntryEntity(DoubleLedgerEntry doubleLedgerEntry) {
        LedgerTransactionEntity ledgerTransactionEntity = LedgerTransactionEntity.builder()
                .description("LedgerService record transaction")
                .referenceId(doubleLedgerEntry.getTransaction().getReferenceId())
                .referenceType(doubleLedgerEntry.getTransaction().getReferenceType().name())
                .orderId(doubleLedgerEntry.getTransaction().getOrderId())
                .idempotencyKey(IdempotencyCreator.createIdempotencyKey(doubleLedgerEntry.getTransaction()))
                .build();

        LedgerEntryEntity creditEntry = LedgerEntryEntity.builder()
                .amount(BigDecimal.valueOf(doubleLedgerEntry.getCredit().getAmount()))
                .accountId(doubleLedgerEntry.getCredit().getAccount().getId())
                .transaction(ledgerTransactionEntity)
                .type(doubleLedgerEntry.getCredit().getType())
                .build();

        LedgerEntryEntity debitEntry = LedgerEntryEntity.builder()
                .amount(BigDecimal.valueOf(doubleLedgerEntry.getDebit().getAmount()))
                .accountId(doubleLedgerEntry.getDebit().getAccount().getId())
                .transaction(ledgerTransactionEntity)
                .type(doubleLedgerEntry.getDebit().getType())
                .build();

        return List.of(creditEntry, debitEntry);
    }
}
