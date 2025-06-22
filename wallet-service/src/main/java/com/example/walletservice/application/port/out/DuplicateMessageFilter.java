package com.example.walletservice.application.port.out;

import com.example.walletservice.domain.PaymentEventMessage;

public interface DuplicateMessageFilter {

    Boolean isAlreadyProcess(PaymentEventMessage paymentEventMessage);
}
