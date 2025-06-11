package com.example.backend.application.service;

import com.example.backend.adapter.out.persistence.exception.PaymentValidationException;
import com.example.backend.adapter.out.web.toss.PaymentExecutorWebAdapter;
import com.example.backend.adapter.out.web.toss.exception.PSPConfirmationException;
import com.example.backend.adapter.out.web.toss.exception.TossPaymentError;
import com.example.backend.adapter.out.web.toss.executor.PaymentExecutor;
import com.example.backend.application.command.CheckoutCommand;
import com.example.backend.application.command.PaymentConfirmCommand;
import com.example.backend.application.port.in.CheckoutUseCase;
import com.example.backend.application.port.out.PaymentStatusUpdatePort;
import com.example.backend.application.port.out.PaymentValidationPort;
import com.example.backend.application.test.PaymentDatabaseHelper;
import com.example.backend.application.test.PaymentTestConfiguration;
import com.example.backend.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@SpringBootTest
@Import(PaymentTestConfiguration.class)
class PaymentConfirmServiceTest {

    @InjectMocks
    private PaymentExecutorWebAdapter mockPaymentExecutor;

    @Mock private PaymentExecutor paymentExecutor;

    private final PaymentDatabaseHelper paymentDatabaseHelper;
    private final CheckoutUseCase checkoutUseCase;
    private final PaymentValidationPort paymentValidationPort;
    private final PaymentStatusUpdatePort paymentStatusUpdatePort;

    PaymentConfirmServiceTest(
            @Autowired PaymentDatabaseHelper paymentDatabaseHelper,
            @Autowired CheckoutUseCase checkoutUseCase,
            @Autowired PaymentValidationPort paymentValidationPort,
            @Autowired PaymentStatusUpdatePort paymentStatusUpdatePort
    ) {
        this.paymentDatabaseHelper = paymentDatabaseHelper;
        this.checkoutUseCase = checkoutUseCase;
        this.paymentValidationPort = paymentValidationPort;
        this.paymentStatusUpdatePort = paymentStatusUpdatePort;
    }

    @BeforeEach
    void setUp() {
        paymentDatabaseHelper.clear();
    }

    @Test
    void should_update_payment_status_to_success_when_payment_confirmed_successfully() {
        String orderId = UUID.randomUUID().toString();

        CheckoutCommand command = new CheckoutCommand(1L, List.of(1L, 2L, 3L), 1L, orderId);

        CheckoutResult checkoutResult = checkoutUseCase.checkout(command).block();

        assertNotNull(checkoutResult);
        PaymentConfirmCommand paymentConfirmCommand = new PaymentConfirmCommand(
                UUID.randomUUID().toString(),
                orderId,
                checkoutResult.getAmount()
        );


        PaymentConfirmService paymentConfirmService = new PaymentConfirmService(
                paymentStatusUpdatePort,
                paymentValidationPort,
                mockPaymentExecutor
        );

        PaymentExecutionResult.PaymentExtraDetails extraDetails = new PaymentExecutionResult.PaymentExtraDetails(
                PaymentType.NORMAL,
                PaymentMethod.EASY_PAY,
                LocalDateTime.now(),
                "test_order_name",
                PSPConfirmationStatus.DONE,
                paymentConfirmCommand.amount(),
                "{}"
        );

        PaymentExecutionResult paymentExecutionResult = new PaymentExecutionResult(
                paymentConfirmCommand.paymentKey(),
                paymentConfirmCommand.orderId(),
                extraDetails,
                null,
                true,   // isSuccess
                false,  // isUnknown
                false   // isFailure
        );

        // When
        when(mockPaymentExecutor.execute(paymentConfirmCommand))
                .thenReturn(Mono.just(paymentExecutionResult));

        PaymentConfirmationResult paymentConfirmationResult = paymentConfirmService
                .confirm(paymentConfirmCommand)
                .block();
        assertNotNull(paymentConfirmationResult);

        // Then
        PaymentEvent paymentEvent = paymentDatabaseHelper.getPayment(orderId);
        assertNotNull(paymentEvent);

        assertThat(paymentConfirmationResult.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
        assertTrue(paymentEvent.isSuccess());
        assertThat(paymentEvent.getPaymentType()).isEqualTo(paymentExecutionResult.getExtraDetails().getPaymentType());
        assertThat(paymentEvent.getPaymentMethod()).isEqualTo(paymentExecutionResult.getExtraDetails().getPaymentMethod());
        assertThat(paymentEvent.getOrderName()).isEqualTo(paymentExecutionResult.getExtraDetails().getOrderName());
        assertThat(paymentEvent.getApprovedAt().truncatedTo(ChronoUnit.MINUTES))
                .isEqualTo(paymentExecutionResult.getExtraDetails().getApprovedAt().truncatedTo(ChronoUnit.MINUTES));

    }

    @Test
    void should_update_payment_status_to_failure_when_payment_confirmed_wrongfully() {
        String orderId = UUID.randomUUID().toString();

        CheckoutCommand command = new CheckoutCommand(1L, List.of(1L, 2L, 3L), 1L, orderId);

        CheckoutResult checkoutResult = checkoutUseCase.checkout(command).block();

        assertNotNull(checkoutResult);
        PaymentConfirmCommand paymentConfirmCommand = new PaymentConfirmCommand(
                UUID.randomUUID().toString(),
                orderId,
                checkoutResult.getAmount()
        );


        PaymentConfirmService paymentConfirmService = new PaymentConfirmService(
                paymentStatusUpdatePort,
                paymentValidationPort,
                mockPaymentExecutor
        );

        PaymentExecutionResult.PaymentExtraDetails extraDetails = new PaymentExecutionResult.PaymentExtraDetails(
                PaymentType.NORMAL,
                PaymentMethod.EASY_PAY,
                LocalDateTime.now(),
                "test_order_name",
                PSPConfirmationStatus.DONE,
                paymentConfirmCommand.amount(),
                "{}"
        );

        PaymentExecutionResult paymentExecutionResult = new PaymentExecutionResult(
                paymentConfirmCommand.paymentKey(),
                paymentConfirmCommand.orderId(),
                extraDetails,
                new PaymentExecutionResult.PaymentFailure("test code", "test message"),
                false,   // isSuccess
                true, // isFailure
                false  // isUnknown
        );

        // When
        when(mockPaymentExecutor.execute(paymentConfirmCommand))
                .thenReturn(Mono.just(paymentExecutionResult));

        PaymentConfirmationResult paymentConfirmationResult = paymentConfirmService
                .confirm(paymentConfirmCommand)
                .block();
        assertNotNull(paymentConfirmationResult);

        // Then
        PaymentEvent paymentEvent = paymentDatabaseHelper.getPayment(orderId);
        assertNotNull(paymentEvent);

        assertThat(paymentConfirmationResult.getStatus()).isEqualTo(PaymentStatus.FAILURE);
        assertTrue(paymentEvent.isFailure());
    }

    @Test
    void should_update_payment_status_to_failure_when_payment_confirmed_unknown_error() {
        String orderId = UUID.randomUUID().toString();

        CheckoutCommand command = new CheckoutCommand(1L, List.of(1L, 2L, 3L), 1L, orderId);

        CheckoutResult checkoutResult = checkoutUseCase.checkout(command).block();

        assertNotNull(checkoutResult);
        PaymentConfirmCommand paymentConfirmCommand = new PaymentConfirmCommand(
                UUID.randomUUID().toString(),
                orderId,
                checkoutResult.getAmount()
        );


        PaymentConfirmService paymentConfirmService = new PaymentConfirmService(
                paymentStatusUpdatePort,
                paymentValidationPort,
                mockPaymentExecutor
        );

        PaymentExecutionResult.PaymentExtraDetails extraDetails = new PaymentExecutionResult.PaymentExtraDetails(
                PaymentType.NORMAL,
                PaymentMethod.EASY_PAY,
                LocalDateTime.now(),
                "test_order_name",
                PSPConfirmationStatus.DONE,
                paymentConfirmCommand.amount(),
                "{}"
        );

        PaymentExecutionResult paymentExecutionResult = new PaymentExecutionResult(
                paymentConfirmCommand.paymentKey(),
                paymentConfirmCommand.orderId(),
                extraDetails,
                new PaymentExecutionResult.PaymentFailure("test code", "test message"),
                false,   // isSuccess
                false, // isFailure
                true  // isUnknown
        );

        // When
        when(mockPaymentExecutor.execute(paymentConfirmCommand))
                .thenReturn(Mono.just(paymentExecutionResult));

        PaymentConfirmationResult paymentConfirmationResult = paymentConfirmService
                .confirm(paymentConfirmCommand)
                .block();
        assertNotNull(paymentConfirmationResult);

        // Then
        PaymentEvent paymentEvent = paymentDatabaseHelper.getPayment(orderId);
        assertNotNull(paymentEvent);

        assertThat(paymentConfirmationResult.getStatus()).isEqualTo(PaymentStatus.UNKNOWN);
        assertTrue(paymentEvent.isUnknown());
    }

    @Test
    void should_handle_PSPConfirmationException() {
        String orderId = UUID.randomUUID().toString();

        CheckoutCommand command = new CheckoutCommand(1L, List.of(1L, 2L, 3L), 1L, orderId);

        CheckoutResult checkoutResult = checkoutUseCase.checkout(command).block();

        assertNotNull(checkoutResult);
        PaymentConfirmCommand paymentConfirmCommand = new PaymentConfirmCommand(
                UUID.randomUUID().toString(),
                orderId,
                checkoutResult.getAmount()
        );


        PaymentConfirmService paymentConfirmService = new PaymentConfirmService(
                paymentStatusUpdatePort,
                paymentValidationPort,
                mockPaymentExecutor
        );

        PSPConfirmationException exception = PSPConfirmationException.builder()
                .message(TossPaymentError.REJECT_ACCOUNT_PAYMENT.getDescription())
                .isSuccess(false)
                .isFailure(true)
                .isUnknown(false)
                .isRetryable(false)
                .build();

        // When
        when(mockPaymentExecutor.execute(paymentConfirmCommand))
                .thenReturn(Mono.error(exception));

        PaymentConfirmationResult paymentConfirmationResult = paymentConfirmService
                .confirm(paymentConfirmCommand)
                .block();
        assertNotNull(paymentConfirmationResult);

        // Then
        PaymentEvent paymentEvent = paymentDatabaseHelper.getPayment(orderId);
        assertNotNull(paymentEvent);

        assertThat(paymentConfirmationResult.getStatus()).isEqualTo(PaymentStatus.FAILURE);
        assertTrue(paymentEvent.isFailure());
    }

    @Test
    void should_handle_PaymentValidationException() {
        String orderId = UUID.randomUUID().toString();

        CheckoutCommand command = new CheckoutCommand(1L, List.of(1L, 2L, 3L), 1L, orderId);

        CheckoutResult checkoutResult = checkoutUseCase.checkout(command).block();

        assertNotNull(checkoutResult);
        PaymentConfirmCommand paymentConfirmCommand = new PaymentConfirmCommand(
                UUID.randomUUID().toString(),
                orderId,
                checkoutResult.getAmount()
        );


        PaymentConfirmService paymentConfirmService = new PaymentConfirmService(
                paymentStatusUpdatePort,
                paymentValidationPort,
                mockPaymentExecutor
        );

        PaymentValidationException exception = new PaymentValidationException("결제 유효셩 검사에서 실패했습니다.");

        // When
        when(mockPaymentExecutor.execute(paymentConfirmCommand))
                .thenReturn(Mono.error(exception));

        PaymentConfirmationResult paymentConfirmationResult = paymentConfirmService
                .confirm(paymentConfirmCommand)
                .block();
        assertNotNull(paymentConfirmationResult);

        // Then
        PaymentEvent paymentEvent = paymentDatabaseHelper.getPayment(orderId);
        assertNotNull(paymentEvent);

        assertThat(paymentConfirmationResult.getStatus()).isEqualTo(PaymentStatus.FAILURE);
        assertTrue(paymentEvent.isFailure());
    }
}