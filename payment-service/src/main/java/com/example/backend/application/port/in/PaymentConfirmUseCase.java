package com.example.backend.application.port.in;

import com.example.backend.application.command.PaymentConfirmCommand;
import com.example.backend.domain.PaymentConfirmationResult;
import reactor.core.publisher.Mono;

public interface PaymentConfirmUseCase {

    Mono<PaymentConfirmationResult> confirm(PaymentConfirmCommand paymentConfirmCommand);
}
