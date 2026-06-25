package net.study.ledgerservice.ledger.adapter.out.persistence.repository;

import net.study.ledgerservice.ledger.adapter.out.persistence.entity.LedgerEntryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JpaLedgerEntryRepository extends JpaRepository<LedgerEntryEntity, Long> {

    List<LedgerEntryEntity> saveAll(List<LedgerEntryEntity> ledgerEntries);
}
