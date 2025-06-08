package com.example.backend.adapter.out.persistence;

import com.example.backend.adapter.out.persistence.repository.PaymentRepository;
import com.example.backend.application.port.out.SavePaymentPort;
import com.example.backend.common.PersistenceAdapter;
import com.example.backend.domain.PaymentEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@PersistenceAdapter
@RequiredArgsConstructor
public class PaymentPersistenceAdapter implements SavePaymentPort {

    private final PaymentRepository paymentRepository;

    @Override
    public Mono<Void> save(PaymentEvent paymentEvent) {
        return paymentRepository.save(paymentEvent);
    }
}
