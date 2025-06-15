package com.example.backend.application.service;

import com.example.backend.application.port.in.PaymentEventMessageRelayUseCase;
import com.example.backend.application.port.out.DispatchEventMessagePort;
import com.example.backend.application.port.out.LoadPendingPaymentEventMessagePort;
import com.example.backend.common.UseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import static java.util.concurrent.TimeUnit.*;

@Slf4j
@UseCase
@Service
@RequiredArgsConstructor
public class PaymentEventMessageRelayService implements PaymentEventMessageRelayUseCase {

    private final LoadPendingPaymentEventMessagePort loadPendingPaymentEventMessagePort;
    private final DispatchEventMessagePort dispatchEventMessagePort;
    private final Scheduler scheduler = Schedulers.newSingle("message-relay");

    @Override
    @Scheduled(fixedDelay = 1, initialDelay = 1, timeUnit = SECONDS)
    public void relay() {
        loadPendingPaymentEventMessagePort.getPendingPaymentEventMessage()
                .map(dispatchEventMessagePort::dispatch)
                .onErrorContinue((err, __) -> log.error("Error occurred while dispatching message-relay", err))
                .subscribeOn(scheduler)
                .subscribe();
    }
}
