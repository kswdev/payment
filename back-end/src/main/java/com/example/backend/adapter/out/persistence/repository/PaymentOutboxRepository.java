package com.example.backend.adapter.out.persistence.repository;

import com.example.backend.application.command.PaymentStatusUpdateCommand;
import com.example.backend.domain.PaymentEventMessage;
import reactor.core.publisher.Mono;

import static com.example.backend.domain.PaymentEventMessage.*;

public interface PaymentOutboxRepository {

    Mono<PaymentEventMessage> insertOutbox(PaymentStatusUpdateCommand command);

    Mono<Boolean> markMessageAsSent(String idempotencyKey, Type paymentEventMessageType);
    Mono<Boolean> markMessageAsFailure(String idempotencyKey, Type paymentEventMessageType);
}
