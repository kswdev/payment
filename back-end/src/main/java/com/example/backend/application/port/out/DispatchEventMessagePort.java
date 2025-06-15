package com.example.backend.application.port.out;

import com.example.backend.domain.PaymentEventMessage;
import reactor.core.publisher.Mono;

public interface DispatchEventMessagePort  {
    Mono<Void> dispatch(PaymentEventMessage paymentEventMessage);
}
