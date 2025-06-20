package com.example.backend.application.port.out;

import com.example.backend.application.command.PaymentStatusUpdateCommand;
import reactor.core.publisher.Mono;

public interface PaymentStatusUpdatePort {

    Mono<Boolean> updatePaymentStatusToExecuting(String orderId, String paymentKey);
    Mono<Boolean> updatePaymentStatus(PaymentStatusUpdateCommand command);
}
