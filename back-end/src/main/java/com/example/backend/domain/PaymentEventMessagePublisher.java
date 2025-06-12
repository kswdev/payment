package com.example.backend.domain;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.reactive.TransactionalEventPublisher;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class PaymentEventMessagePublisher {

    private final TransactionalEventPublisher publisher;

    public Mono<PaymentEventMessage> publicEvent(PaymentEventMessage eventMessage) {
        return publisher
                .publishEvent(eventMessage)
                .thenReturn(eventMessage);
    }
}
