package com.example.backend.application.port.in;

import com.example.backend.domain.LedgerEventMessage;
import com.example.backend.domain.WalletEventMessage;
import reactor.core.publisher.Mono;

public interface PaymentCompleteUseCase {

    Mono<Void> completePayment(WalletEventMessage walletEventMessage);
    Mono<Void> completePayment(LedgerEventMessage ledgerEventMessage);
}
