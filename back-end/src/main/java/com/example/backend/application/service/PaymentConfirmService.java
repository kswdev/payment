package com.example.backend.application.service;

import com.example.backend.adapter.out.persistence.exception.PaymentAlreadyProcessedException;
import com.example.backend.adapter.out.persistence.exception.PaymentValidationException;
import com.example.backend.adapter.out.web.toss.exception.PSPConfirmationException;
import com.example.backend.application.command.PaymentConfirmCommand;
import com.example.backend.application.command.PaymentStatusUpdateCommand;
import com.example.backend.application.port.in.PaymentConfirmUseCase;
import com.example.backend.application.port.out.PaymentExecutorPort;
import com.example.backend.application.port.out.PaymentStatusUpdatePort;
import com.example.backend.application.port.out.PaymentValidationPort;
import com.example.backend.common.UseCase;
import com.example.backend.domain.PaymentConfirmationResult;
import com.example.backend.domain.PaymentExecutionResult;
import com.example.backend.domain.PaymentStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.concurrent.TimeoutException;
import java.util.function.Function;

@UseCase
@Service
@RequiredArgsConstructor
public class PaymentConfirmService implements PaymentConfirmUseCase {

    private final PaymentStatusUpdatePort paymentStatusUpdatePort;
    private final PaymentValidationPort paymentValidationPort;
    private final PaymentExecutorPort paymentExecutorPort;

    @Override
    public Mono<PaymentConfirmationResult> confirm(PaymentConfirmCommand command) {
        return validateAndExecutePayment(command)
                .flatMap(this::updatePaymentStatusAndMapResult)
                .onErrorResume(handlePaymentError(command));
    }

    private Mono<PaymentExecutionResult> validateAndExecutePayment(PaymentConfirmCommand command) {
        return paymentStatusUpdatePort.updatePaymentStatusToExecuting(command.orderId(), command.paymentKey())
                .filterWhen(__ -> paymentValidationPort.isValid(command.orderId(), command.amount()))
                .then(paymentExecutorPort.execute(command));
    }

    private Mono<PaymentConfirmationResult> updatePaymentStatusAndMapResult(PaymentExecutionResult executionResult) {
        return paymentStatusUpdatePort.updatePaymentStatus(PaymentStatusUpdateCommand.from(executionResult))
                .thenReturn(new PaymentConfirmationResult(
                        executionResult.paymentStatus(),
                        executionResult.getPaymentFailure()
                ));
    }

    private Function<Throwable, Mono<? extends PaymentConfirmationResult>> handlePaymentError(PaymentConfirmCommand command) {
        return error -> {
            if (error instanceof PaymentAlreadyProcessedException exception) {
                return handlePaymentAlreadyProcessed(exception);
            }
            return handleGeneralError(command, error);
        };
    }

    private Mono<PaymentConfirmationResult> handlePaymentAlreadyProcessed(PaymentAlreadyProcessedException exception) {
        return Mono.just(new PaymentConfirmationResult(
                exception.getPaymentStatus(),
                new PaymentExecutionResult.PaymentFailure(
                        exception.getPaymentStatus().name(),
                        exception.getMessage()
                )
        ));
    }

    private Mono<PaymentConfirmationResult> handleGeneralError(PaymentConfirmCommand command, Throwable error) {
        Pair<PaymentStatus, PaymentExecutionResult.PaymentFailure> result = getErrorResult(error);
        return updatePaymentStatusAndReturnResult(command, result);
    }

    private Mono<PaymentConfirmationResult> updatePaymentStatusAndReturnResult(
            PaymentConfirmCommand command,
            Pair<PaymentStatus, PaymentExecutionResult.PaymentFailure> result
    ) {
        PaymentStatusUpdateCommand updateCommand = PaymentStatusUpdateCommand.builder()
                .paymentKey(command.paymentKey())
                .orderId(command.orderId())
                .status(result.getFirst())
                .paymentFailure(result.getSecond())
                .build();

        return paymentStatusUpdatePort.updatePaymentStatus(updateCommand)
                .thenReturn(new PaymentConfirmationResult(result.getFirst(), result.getSecond()));
    }


    private Pair<PaymentStatus, PaymentExecutionResult.PaymentFailure> getErrorResult(Throwable error) {
        return switch (error) {
            case PSPConfirmationException exception ->
                    Pair.of(exception.paymentStatus(),
                            new PaymentExecutionResult.PaymentFailure(exception.getErrorCode(), error.getMessage()));
            case PaymentValidationException exception ->
                    Pair.of(PaymentStatus.FAILURE,
                            new PaymentExecutionResult.PaymentFailure(exception.getClass().getSimpleName(), exception.getMessage()));
            case TimeoutException exception ->
                    Pair.of(PaymentStatus.UNKNOWN,
                            new PaymentExecutionResult.PaymentFailure(exception.getClass().getSimpleName(), exception.getMessage()));
            default ->
                    Pair.of(PaymentStatus.UNKNOWN,
                            new PaymentExecutionResult.PaymentFailure(error.getClass().getSimpleName(), error.getMessage()));
        };
    }

}
