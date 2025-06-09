package com.example.backend.application.service;

import com.example.backend.application.command.PaymentConfirmCommand;
import com.example.backend.application.command.PaymentStatusUpdateCommand;
import com.example.backend.application.port.in.PaymentConfirmUseCase;
import com.example.backend.application.port.out.PaymentExecutorPort;
import com.example.backend.application.port.out.PaymentStatusUpdatePort;
import com.example.backend.application.port.out.PaymentValidationPort;
import com.example.backend.common.UseCase;
import com.example.backend.domain.PaymentConfirmationResult;
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

    @Override
    public Mono<PaymentConfirmationResult> confirm(PaymentConfirmCommand command) {
        return paymentStatusUpdatePort.updatePaymentStatusToExecuting(command.orderId(), command.paymentKey())
                .filterWhen(__ -> paymentValidationPort.isValid(command.orderId(), command.amount()))
                .then(paymentExecutorPort.execute(command))
                .flatMap(paymentExecutionResult ->  paymentStatusUpdatePort.updatePaymentStatus(
                        PaymentStatusUpdateCommand.from(paymentExecutionResult)
                    ).thenReturn(paymentExecutionResult)
                )
                .flatMap(result -> Mono.just(new PaymentConfirmationResult(result.paymentStatus(), result.getPaymentFailure())));
    }
}
