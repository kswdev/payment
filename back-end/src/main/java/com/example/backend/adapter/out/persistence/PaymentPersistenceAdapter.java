package com.example.backend.adapter.out.persistence;

import com.example.backend.adapter.out.persistence.repository.PaymentRepository;
import com.example.backend.adapter.out.persistence.repository.PaymentStatusUpdateRepository;
import com.example.backend.adapter.out.persistence.repository.PaymentValidationRepository;
import com.example.backend.application.command.PaymentStatusUpdateCommand;
import com.example.backend.application.port.out.PaymentStatusUpdatePort;
import com.example.backend.application.port.out.PaymentValidationPort;
import com.example.backend.application.port.out.SavePaymentPort;
import com.example.backend.common.PersistenceAdapter;
import com.example.backend.domain.PaymentEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@PersistenceAdapter
@RequiredArgsConstructor
public class PaymentPersistenceAdapter implements
        SavePaymentPort,
        PaymentStatusUpdatePort,
        PaymentValidationPort {

    private final PaymentRepository paymentRepository;
    private final PaymentStatusUpdateRepository paymentStatusUpdateRepository;
    private final PaymentValidationRepository paymentValidationRepository;

    @Override
    public Mono<Void> save(PaymentEvent paymentEvent) {
        return paymentRepository.save(paymentEvent);
    }

    @Override
    public Mono<Boolean> updatePaymentStatusToExecuting(String orderId, String paymentKey) {
        return paymentStatusUpdateRepository.updatePaymentStatusToExecuting(orderId, paymentKey);
    }

    @Override
    public Mono<Boolean> updatePaymentStatus(PaymentStatusUpdateCommand command) {
        return paymentStatusUpdateRepository.updatePaymentStatus(command);
    }

    @Override
    public Mono<Boolean> isValid(String orderId, Long amount) {
        return paymentValidationRepository.isValid(orderId, amount);
    }
}
