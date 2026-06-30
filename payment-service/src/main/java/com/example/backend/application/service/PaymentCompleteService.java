package com.example.backend.application.service;

import com.example.backend.application.port.in.PaymentCompleteUseCase;
import com.example.backend.application.port.out.CompletePaymentPort;
import com.example.backend.application.port.out.LoadPaymentPort;
import com.example.backend.common.UseCase;
import com.example.backend.domain.LedgerEventMessage;
import com.example.backend.domain.WalletEventMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@UseCase
@Service
@RequiredArgsConstructor
public class PaymentCompleteService implements PaymentCompleteUseCase {

    private final LoadPaymentPort loadPaymentPort;
    private final CompletePaymentPort completePaymentPort;

    @Override
    public Mono<Void> completePayment(WalletEventMessage walletEventMessage) {
        return loadPaymentPort.getPayment(walletEventMessage.orderId())
                .map(paymentEvent -> {
                    paymentEvent.confirmWalletUpdate();
                    return paymentEvent;
                })
                .map(paymentEvent -> {
                    paymentEvent.completeIfDone();
                    return paymentEvent;
                }).flatMap(completePaymentPort::complete);
    }

    @Override
    public Mono<Void> completePayment(LedgerEventMessage ledgerEventMessage) {
        return loadPaymentPort.getPayment(ledgerEventMessage.orderId())
                .map(paymentEvent -> {
                    paymentEvent.confirmLedgerUpdate();
                    return paymentEvent;
                })
                .map(paymentEvent -> {
                    paymentEvent.completeIfDone();
                    return paymentEvent;
                }).flatMap(completePaymentPort::complete);
    }
}
