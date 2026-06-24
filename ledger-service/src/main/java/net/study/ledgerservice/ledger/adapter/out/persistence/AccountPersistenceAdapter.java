package net.study.ledgerservice.ledger.adapter.out.persistence;

import lombok.RequiredArgsConstructor;
import net.study.ledgerservice.common.PersistenceAdapter;
import net.study.ledgerservice.ledger.adapter.out.persistence.entity.AccountEntity;
import net.study.ledgerservice.ledger.adapter.out.persistence.repository.JpaAccountRepository;
import net.study.ledgerservice.ledger.application.port.out.LoadAccountPort;
import net.study.ledgerservice.ledger.domain.Account;

@RequiredArgsConstructor
@PersistenceAdapter
public class AccountPersistenceAdapter implements LoadAccountPort {

    private final JpaAccountRepository accountRepository;

    @Override
    public Account getAccount(String name) {
        AccountEntity accountEntity = accountRepository.findByName(name);
        return new Account(accountEntity.getId(), accountEntity.getName());
    }
}
