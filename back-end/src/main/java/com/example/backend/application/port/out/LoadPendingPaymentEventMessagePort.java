package com.example.backend.application.port.out;

import com.example.backend.domain.PaymentEventMessage;
import reactor.core.publisher.Flux;

public interface LoadPendingPaymentEventMessagePort {

    Flux<PaymentEventMessage> getPendingPaymentEventMessage();
}
