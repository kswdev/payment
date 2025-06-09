package com.example.backend.application.port.out;

import reactor.core.publisher.Mono;

public interface  PaymentValidationPort {

    Mono<Boolean> isValid(String orderId, Long amount);
}
