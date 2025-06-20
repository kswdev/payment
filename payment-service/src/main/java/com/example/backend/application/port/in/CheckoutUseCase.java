package com.example.backend.application.port.in;

import com.example.backend.application.command.CheckoutCommand;
import com.example.backend.domain.CheckoutResult;
import reactor.core.publisher.Mono;

public interface CheckoutUseCase {
    Mono<CheckoutResult> checkout(CheckoutCommand command);
}
