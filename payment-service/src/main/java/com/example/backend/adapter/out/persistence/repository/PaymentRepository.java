package com.example.backend.adapter.out.persistence.repository;

import com.example.backend.domain.PaymentEvent;
import com.example.backend.domain.PendingPaymentEvent;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PaymentRepository {

    Mono<Void> save(PaymentEvent paymentEvent);
    Flux<PendingPaymentEvent> getPendingPayments();
}
