package com.example.backend.application.service;

import com.example.backend.adapter.out.persistence.repository.PaymentOutboxRepository;
import com.example.backend.application.command.PaymentConfirmCommand;
import com.example.backend.application.command.PaymentStatusUpdateCommand;
import com.example.backend.application.port.in.PaymentEventMessageRelayUseCase;
import com.example.backend.domain.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Hooks;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PaymentEventMessageRelayServiceTest {

    private final PaymentOutboxRepository paymentOutboxRepository;
    private final PaymentEventMessageRelayUseCase paymentEventMessageRelayUseCase;

    public PaymentEventMessageRelayServiceTest(
            @Autowired PaymentOutboxRepository paymentOutboxRepository,
            @Autowired PaymentEventMessageRelayUseCase paymentEventMessageRelayUseCase)
    {
        this.paymentOutboxRepository = paymentOutboxRepository;
        this.paymentEventMessageRelayUseCase = paymentEventMessageRelayUseCase;
    }


    @Test
    void should_dispatch_external_message_system() throws InterruptedException {
        Hooks.onOperatorDebug();

        PaymentStatusUpdateCommand paymentStatusUpdateCommand = createPaymentStatusUpdateCommand();

        paymentOutboxRepository.insertOutbox(paymentStatusUpdateCommand).block();
        paymentEventMessageRelayUseCase.relay();

        Thread.sleep(10000);

    }

    private PaymentStatusUpdateCommand createPaymentStatusUpdateCommand() {
        return new PaymentStatusUpdateCommand(
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                PaymentStatus.SUCCESS,
                createPaymentExtraDetails(),
                null
        );
    }

    private PaymentExecutionResult.PaymentExtraDetails createPaymentExtraDetails() {
        return new PaymentExecutionResult.PaymentExtraDetails(
                PaymentType.NORMAL,
                PaymentMethod.EASY_PAY,
                LocalDateTime.now(),
                "test_order_name",
                PSPConfirmationStatus.DONE,
                4000L,
                "{}"
        );
    }
}