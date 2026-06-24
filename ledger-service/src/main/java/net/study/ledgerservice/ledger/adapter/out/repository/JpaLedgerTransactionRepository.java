package net.study.ledgerservice.ledger.adapter.out.repository;

import net.study.ledgerservice.ledger.adapter.out.entity.LedgerTransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaLedgerTransactionRepository extends JpaRepository<LedgerTransactionEntity, Long> {

    Boolean isExistByOrderId(String orderId);
}
