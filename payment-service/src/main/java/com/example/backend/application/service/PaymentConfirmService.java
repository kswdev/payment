package com.example.backend.application.service;

import com.example.backend.adapter.out.persistence.exception.PaymentValidationException;
import com.example.backend.application.command.PaymentConfirmCommand;
import com.example.backend.application.command.PaymentStatusUpdateCommand;
import com.example.backend.application.port.in.PaymentConfirmUseCase;
import com.example.backend.application.port.out.PaymentExecutorPort;
import com.example.backend.application.port.out.PaymentStatusUpdatePort;
import com.example.backend.application.port.out.PaymentValidationPort;
import com.example.backend.common.UseCase;
import com.example.backend.domain.PaymentConfirmationResult;
import com.example.backend.domain.PaymentExecutionResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@UseCase
@Service
@RequiredArgsConstructor
public class PaymentConfirmService implements PaymentConfirmUseCase {

    private final PaymentStatusUpdatePort paymentStatusUpdatePort;
    private final PaymentValidationPort paymentValidationPort;
    private final PaymentExecutorPort paymentExecutorPort;

    private final PaymentErrorHandler errorHandler;

    @Override
    public Mono<PaymentConfirmationResult> confirm(PaymentConfirmCommand command) {
        return paymentStatusUpdatePort.updatePaymentStatusToExecuting(command.orderId(), command.paymentKey())
                .then(validatePayment(command)).thenReturn(command)
                .flatMap(paymentExecutorPort::execute)
                .flatMap(this::updatePaymentStatusAndMapResult)
                .onErrorResume(errorHandler.handlePaymentConfirmationError(command));
    }

    private Mono<Void> validatePayment(PaymentConfirmCommand command) {
        return paymentValidationPort.isValid(command.orderId(), command.amount())
                .flatMap(isValid -> isValid
                        ? Mono.empty()
                        : Mono.error(new PaymentValidationException("Payment validation failed")));
    }

    private Mono<PaymentConfirmationResult> updatePaymentStatusAndMapResult(PaymentExecutionResult executionResult) {
        return paymentStatusUpdatePort.updatePaymentStatus(PaymentStatusUpdateCommand.from(executionResult))
                .thenReturn(new PaymentConfirmationResult(
                        executionResult.paymentStatus(),
                        executionResult.getPaymentFailure()
                ));
    }
}
