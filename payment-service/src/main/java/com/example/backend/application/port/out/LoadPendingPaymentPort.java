package com.example.backend.application.port.out;

import com.example.backend.domain.PendingPaymentEvent;
import reactor.core.publisher.Flux;

public interface LoadPendingPaymentPort {

    Flux<PendingPaymentEvent> getPendingPayments();
}
