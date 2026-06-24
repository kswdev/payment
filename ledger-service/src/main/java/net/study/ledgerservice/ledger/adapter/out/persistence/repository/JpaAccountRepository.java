package net.study.ledgerservice.ledger.adapter.out.persistence.repository;

import net.study.ledgerservice.ledger.adapter.out.persistence.entity.AccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaAccountRepository extends JpaRepository<AccountEntity, Long> {

    AccountEntity findByName(String name);
}
