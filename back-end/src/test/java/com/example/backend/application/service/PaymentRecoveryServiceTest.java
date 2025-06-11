package com.example.backend.application.service;

import com.example.backend.application.command.CheckoutCommand;
import com.example.backend.application.command.PaymentConfirmCommand;
import com.example.backend.application.command.PaymentStatusUpdateCommand;
import com.example.backend.application.port.in.CheckoutUseCase;
import com.example.backend.application.port.out.LoadPendingPaymentPort;
import com.example.backend.application.port.out.PaymentExecutorPort;
import com.example.backend.application.port.out.PaymentStatusUpdatePort;
import com.example.backend.application.port.out.PaymentValidationPort;
import com.example.backend.application.test.PaymentDatabaseHelper;
import com.example.backend.application.test.PaymentTestConfiguration;
import com.example.backend.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;


@SpringBootTest
@ExtendWith(MockitoExtension.class)
@Import(PaymentTestConfiguration.class)
class PaymentRecoveryServiceTest {

    @Mock private PaymentExecutorPort mockPaymentExecutor;

    @Autowired private PaymentDatabaseHelper paymentDatabaseHelper;
    @Autowired private LoadPendingPaymentPort loadPendingPaymentPort;
    @Autowired private PaymentStatusUpdatePort paymentStatusUpdatePort;
    @Autowired private PaymentValidationPort paymentValidationPort;
    @Autowired private CheckoutUseCase checkoutUseCase;

    @Autowired private PaymentErrorHandler paymentErrorHandler;

    private PaymentRecoveryService paymentRecoveryService;

    @BeforeEach
    void setUp() {
        paymentDatabaseHelper.clear();

        paymentRecoveryService = new PaymentRecoveryService(
                loadPendingPaymentPort,
                paymentStatusUpdatePort,
                paymentValidationPort,
                mockPaymentExecutor,
                paymentErrorHandler
        );
    }


    @Test
    void should_recovery_payments() {
        // given
        PaymentConfirmCommand paymentCommand = createPaymentCommandWithUnknownStatus();
        PaymentExecutionResult expectedResult = createSuccessfulPaymentResult(paymentCommand);

        // when
        stubPaymentExecutorResponse(paymentCommand, expectedResult);

        // then
        paymentRecoveryService.recoverPayments()
                .as(StepVerifier::create)
                .expectComplete()
                .verify(Duration.ofSeconds(1));
    }

    private PaymentConfirmCommand createPaymentCommandWithUnknownStatus() {
        // 1. Checkout 생성
        CheckoutCommand checkoutCommand = createCheckoutCommand();
        CheckoutResult checkoutResult = executeCheckout(checkoutCommand);

        // 2. Payment 생성
        PaymentConfirmCommand paymentCommand = new PaymentConfirmCommand(
                UUID.randomUUID().toString(),  // paymentKey
                checkoutCommand.idempotencyKey(),  // orderId
                checkoutResult.getAmount()
        );

        // 3. Payment 상태를 UNKNOWN으로 설정
        updatePaymentToUnknownStatus(paymentCommand);

        return paymentCommand;
    }

    private CheckoutCommand createCheckoutCommand() {
        return new CheckoutCommand(
                1L,                          // cartId
                List.of(1L, 2L),            // productIds
                1L,                          // buyerId
                UUID.randomUUID().toString() // idempotencyKey
        );
    }

    private CheckoutResult executeCheckout(CheckoutCommand command) {
        return checkoutUseCase.checkout(command)
                .block();
    }

    private void updatePaymentToUnknownStatus(PaymentConfirmCommand command) {
        // EXECUTING 상태로 업데이트
        paymentStatusUpdatePort.updatePaymentStatusToExecuting(
                command.orderId(),
                command.paymentKey()
        ).block();

        // UNKNOWN 상태로 업데이트
        PaymentStatusUpdateCommand updateCommand = new PaymentStatusUpdateCommand(
                command.paymentKey(),
                command.orderId(),
                PaymentStatus.UNKNOWN,
                null,
                new PaymentFailure("UNKNOWN", "UNKNOWN")
        );

        paymentStatusUpdatePort.updatePaymentStatus(updateCommand)
                .block();
    }

    private PaymentExecutionResult createSuccessfulPaymentResult(PaymentConfirmCommand command) {
        return PaymentExecutionResult.builder()
                .paymentKey(command.paymentKey())
                .orderId(command.orderId())
                .extraDetails(createPaymentExtraDetails(command))
                .isSuccess(true)
                .isFailure(false)
                .isUnknown(false)
                .build();
    }

    private PaymentExecutionResult.PaymentExtraDetails createPaymentExtraDetails(PaymentConfirmCommand command) {
        return new PaymentExecutionResult.PaymentExtraDetails(
                PaymentType.NORMAL,
                PaymentMethod.EASY_PAY,
                LocalDateTime.now(),
                "test_order_name",
                PSPConfirmationStatus.DONE,
                command.amount(),
                "{}"
        );
    }

    private void stubPaymentExecutorResponse(PaymentConfirmCommand command, PaymentExecutionResult result) {
        when(mockPaymentExecutor.execute(command))
                .thenReturn(Mono.just(result));
    }

}
