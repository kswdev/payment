package com.example.backend.application.port.out;

import com.example.backend.application.command.PaymentConfirmCommand;
import com.example.backend.domain.PaymentExecutionResult;
import reactor.core.publisher.Mono;

public interface PaymentExecutorPort {
    Mono<PaymentExecutionResult> execute(PaymentConfirmCommand command);
}
