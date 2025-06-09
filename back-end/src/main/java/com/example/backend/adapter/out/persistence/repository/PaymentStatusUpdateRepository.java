package com.example.backend.adapter.out.persistence.repository;

import com.example.backend.application.command.PaymentStatusUpdateCommand;
import reactor.core.publisher.Mono;

public interface PaymentStatusUpdateRepository {

    Mono<Boolean> updatePaymentStatusToExecuting(String orderId, String paymentKey);
    Mono<Boolean> updatePaymentStatus(PaymentStatusUpdateCommand command);
}
