package com.example.backend.application.port.out;

import com.example.backend.domain.PaymentEvent;
import reactor.core.publisher.Mono;

public interface CompletePaymentPort {
    Mono<Void> complete(PaymentEvent paymentEvent);
}
