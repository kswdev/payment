package net.study.ledgerservice.ledger.adapter.out.persistence.repository;

import net.study.ledgerservice.ledger.adapter.out.persistence.entity.LedgerTransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaLedgerTransactionRepository extends JpaRepository<LedgerTransactionEntity, Long> {

    Boolean isExistByOrderId(String orderId);
}
