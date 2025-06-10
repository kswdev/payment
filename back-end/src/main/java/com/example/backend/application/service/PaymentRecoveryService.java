package com.example.backend.application.service;

import com.example.backend.application.command.PaymentConfirmCommand;
import com.example.backend.application.command.PaymentStatusUpdateCommand;
import com.example.backend.application.port.in.PaymentRecoveryUseCase;
import com.example.backend.application.port.out.LoadPendingPaymentPort;
import com.example.backend.application.port.out.PaymentExecutorPort;
import com.example.backend.application.port.out.PaymentStatusUpdatePort;
import com.example.backend.application.port.out.PaymentValidationPort;
import com.example.backend.common.UseCase;
import com.example.backend.domain.PaymentExecutionResult;
import com.example.backend.domain.PendingPaymentEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Signal;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@UseCase
@RequiredArgsConstructor
public class PaymentRecoveryService implements PaymentRecoveryUseCase {

    private final LoadPendingPaymentPort loadPendingPaymentPort;
    private final PaymentStatusUpdatePort paymentStatusUpdatePort;
    private final PaymentValidationPort paymentValidationPort;
    private final PaymentExecutorPort paymentExecutorPort;
    private final Scheduler scheduler = Schedulers.newSingle("payment-recovery");

    // 시스템에 영향을 끼치지 않기 위해 다른 스레드를 사용해 격리(bulk head)
    @Override
    @Scheduled(fixedDelay = 100, timeUnit = TimeUnit.SECONDS)
    public void recovery() {

        loadPendingPaymentsAndMapToConfirmCommand()
                .parallel()
                .runOn(Schedulers.parallel())
                .flatMap(this::validatePaymentAndExecutePayment)
                .flatMap(this::updatePaymentStatus)
                .sequential()
                .doOnEach(this::logPaymentResult)
                .subscribeOn(scheduler)
                .subscribe();
    }

    private Flux<PaymentConfirmCommand> loadPendingPaymentsAndMapToConfirmCommand() {
        return loadPendingPaymentPort.getPendingPayments()
                .map(this::toPaymentConfirmCommand);
    }

    private PaymentConfirmCommand toPaymentConfirmCommand(PendingPaymentEvent event) {
        return new PaymentConfirmCommand(
                event.getPaymentKey(),
                event.getOrderId(),
                event.getTotalAmount()
        );
    }

    private Mono<PaymentExecutionResult> validatePaymentAndExecutePayment(PaymentConfirmCommand command) {
        return validatePayment(command)
                .flatMap(paymentExecutorPort::execute);
    }

    private Mono<PaymentConfirmCommand> validatePayment(PaymentConfirmCommand command) {
        return paymentValidationPort.isValid(command.orderId(), command.amount())
                .thenReturn(command);
    }

    private Mono<PaymentExecutionResult> updatePaymentStatus(PaymentExecutionResult result) {
        return paymentStatusUpdatePort.updatePaymentStatus(
                PaymentStatusUpdateCommand.from(result)
        ).thenReturn(result);
    }

    private void logPaymentResult(Signal<PaymentExecutionResult> signal) {
        if (signal.get() != null) {
            String orderId = signal.get().getOrderId();
            if (signal.isOnNext() && signal.isOnComplete()) {
                log.info("successfully recovered payment: {}", orderId);
            } else {
                log.error("failed to recover payment: {}", orderId);
            }
        }
    }
}
