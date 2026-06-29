package com.example.backend.application.service;

import com.example.backend.application.port.in.PaymentCompleteUseCase;
import com.example.backend.common.UseCase;
import com.example.backend.domain.LedgerEventMessage;
import com.example.backend.domain.WalletEventMessage;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@UseCase
@Service
public class PaymentCompleteService implements PaymentCompleteUseCase {

    @Override
    public Mono<Void> completePayment(WalletEventMessage walletEventMessage) {
        return null;
    }

    @Override
    public Mono<Void> completePayment(LedgerEventMessage ledgerEventMessage) {
        return null;
    }
}
