package com.example.backend.application.service;

import com.example.backend.adapter.out.persistence.exception.PaymentAlreadyProcessedException;
import com.example.backend.adapter.out.persistence.exception.PaymentValidationException;
import com.example.backend.adapter.out.web.toss.exception.PSPConfirmationException;
import com.example.backend.application.command.PaymentConfirmCommand;
import com.example.backend.application.command.PaymentStatusUpdateCommand;
import com.example.backend.application.port.out.PaymentStatusUpdatePort;
import com.example.backend.domain.PaymentConfirmationResult;
import com.example.backend.domain.PaymentFailure;
import com.example.backend.domain.PaymentStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.concurrent.TimeoutException;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class PaymentErrorHandler {

    private final PaymentStatusUpdatePort paymentStatusUpdatePort;

    public Function<Throwable, Mono<PaymentConfirmationResult>> handlePaymentConfirmationError(PaymentConfirmCommand command) {
        return error -> switch (error) {
            case PaymentAlreadyProcessedException exception ->
                    Mono.just(createPaymentConfirmationResult(exception.getPaymentStatus(),
                            createPaymentFailure(exception.getPaymentStatus().name(), exception.getMessage())));

            case PSPConfirmationException exception ->
                    handleErrorWithStatusUpdate(command, exception.paymentStatus(),
                            createPaymentFailure(exception.getErrorCode(), exception.getMessage()));

            case PaymentValidationException exception ->
                    handleErrorWithStatusUpdate(command, PaymentStatus.FAILURE,
                            createPaymentFailure(exception.getClass().getSimpleName(), exception.getMessage()));

            case TimeoutException exception ->
                    handleErrorWithStatusUpdate(command, PaymentStatus.UNKNOWN,
                            createPaymentFailure(exception.getClass().getSimpleName(), exception.getMessage()));

            default ->
                    handleErrorWithStatusUpdate(command, PaymentStatus.UNKNOWN,
                            createPaymentFailure(error.getClass().getSimpleName(), error.getMessage()));
        };
    }

    private Mono<PaymentConfirmationResult> handleErrorWithStatusUpdate(
            PaymentConfirmCommand command,
            PaymentStatus status,
            PaymentFailure failure) {

        PaymentStatusUpdateCommand updateCommand = createStatusUpdateCommand(command, status, failure);

        return paymentStatusUpdatePort.updatePaymentStatus(updateCommand)
                .thenReturn(createPaymentConfirmationResult(status, failure));
    }

    private PaymentStatusUpdateCommand createStatusUpdateCommand(
            PaymentConfirmCommand command,
            PaymentStatus status,
            PaymentFailure failure) {

        return PaymentStatusUpdateCommand.builder()
                .paymentKey(command.paymentKey())
                .orderId(command.orderId())
                .status(status)
                .paymentFailure(failure)
                .build();
    }

    private PaymentConfirmationResult createPaymentConfirmationResult(PaymentStatus status, PaymentFailure failure) {
        return new PaymentConfirmationResult(status, failure);
    }

    private PaymentFailure createPaymentFailure(String errorCode, String message) {
        return new PaymentFailure(errorCode, message);
    }

}
