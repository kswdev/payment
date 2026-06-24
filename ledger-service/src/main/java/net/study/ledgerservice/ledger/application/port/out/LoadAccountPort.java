package net.study.ledgerservice.ledger.application.port.out;

import net.study.ledgerservice.ledger.domain.Account;

public interface LoadAccountPort {

    Account getAccount(String name);
}
