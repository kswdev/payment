package com.example.backend.adapter.out.web.toss.executor;

import com.example.backend.application.command.PaymentConfirmCommand;
import com.example.backend.domain.PaymentExecutionResult;
import reactor.core.publisher.Mono;

public interface PaymentExecutor {

    Mono<PaymentExecutionResult> execute(PaymentConfirmCommand command);
}
