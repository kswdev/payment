package net.study.ledgerservice.ledger.application.port.out;

import net.study.ledgerservice.ledger.domain.PaymentEventMessage;

public interface DuplicateMessageFilterPort {

    Boolean isAlreadyProcess(PaymentEventMessage message);
}
