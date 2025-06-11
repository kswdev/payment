package com.example.backend.application.service;

import com.example.backend.adapter.out.persistence.exception.PaymentValidationException;
import com.example.backend.adapter.out.web.toss.PaymentExecutorWebAdapter;
import com.example.backend.adapter.out.web.toss.exception.PSPConfirmationException;
import com.example.backend.adapter.out.web.toss.exception.TossPaymentError;
import com.example.backend.application.command.CheckoutCommand;
import com.example.backend.application.command.PaymentConfirmCommand;
import com.example.backend.application.port.in.CheckoutUseCase;
import com.example.backend.application.port.out.PaymentStatusUpdatePort;
import com.example.backend.application.port.out.PaymentValidationPort;
import com.example.backend.application.test.PaymentDatabaseHelper;
import com.example.backend.application.test.PaymentTestConfiguration;
import com.example.backend.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
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

    @Mock private PaymentExecutorWebAdapter mockPaymentExecutor;

    @Autowired private CheckoutUseCase checkoutUseCase;
    @Autowired private PaymentDatabaseHelper paymentDatabaseHelper;
    @Autowired private PaymentValidationPort paymentValidationPort;
    @Autowired private PaymentStatusUpdatePort paymentStatusUpdatePort;

    @Autowired private PaymentErrorHandler paymentErrorHandler;

    private PaymentConfirmService paymentConfirmService;
    private String orderId;
    private PaymentConfirmCommand paymentConfirmCommand;
    private CheckoutResult checkoutResult;

    @BeforeEach
    void setUp() {
        paymentDatabaseHelper.clear();
        paymentConfirmService = new PaymentConfirmService(
                paymentStatusUpdatePort,
                paymentValidationPort,
                mockPaymentExecutor,
                paymentErrorHandler
        );

        // 기본 데이터 생성
        orderId = UUID.randomUUID().toString();
        CheckoutCommand checkoutCommand = new CheckoutCommand(1L, List.of(1L, 2L, 3L), 1L, orderId);
        checkoutResult = checkoutUseCase.checkout(checkoutCommand).block();

        assertNotNull(checkoutResult);

        paymentConfirmCommand = new PaymentConfirmCommand(
                UUID.randomUUID().toString(),
                orderId,
                checkoutResult.getAmount()
        );
    }


    @Nested
    @DisplayName("[PaymentConfirmService]결제 성공 테스트")
    class SuccessfulPaymentTests {

        @Test
        @DisplayName("결제가 성공적으로 확인되면 결제 상태를 SUCCESS로 업데이트해야 함")
        void testSuccessfulPaymentConfirmation() {
            // given
            PaymentExecutionResult successResult = createExecutionResult(
                    true,   // isSuccess
                    false,  // isFailure
                    false,  // isUnknown
                    null    // no failure
            );

            // when
            when(mockPaymentExecutor.execute(paymentConfirmCommand))
                    .thenReturn(Mono.just(successResult));

            PaymentConfirmationResult result = paymentConfirmService
                    .confirm(paymentConfirmCommand)
                    .block();

            // then
            verifyPaymentResult(result, PaymentStatus.SUCCESS, true, false, false);

            PaymentEvent paymentEvent = paymentDatabaseHelper.getPayment(orderId);
            assertThat(paymentEvent.getPaymentType()).isEqualTo(successResult.getExtraDetails().getPaymentType());
            assertThat(paymentEvent.getPaymentMethod()).isEqualTo(successResult.getExtraDetails().getPaymentMethod());
            assertThat(paymentEvent.getOrderName()).isEqualTo(successResult.getExtraDetails().getOrderName());
            assertThat(paymentEvent.getApprovedAt().truncatedTo(ChronoUnit.MINUTES))
                    .isEqualTo(successResult.getExtraDetails().getApprovedAt().truncatedTo(ChronoUnit.MINUTES));
        }
    }

    @Nested
    @DisplayName("[PaymentConfirmService]결제 실패 테스트")
    class FailedPaymentTests {

        @Test
        @DisplayName("결제 확인이 실패하면 결제 상태를 FAILURE로 업데이트해야 함")
        void testFailedPaymentConfirmation() {
            // given
            PaymentFailure failure = new PaymentFailure("test code", "test message");
            PaymentExecutionResult failureResult = createExecutionResult(
                    false,  // isSuccess
                    true,   // isFailure
                    false,  // isUnknown
                    failure
            );

            // when
            when(mockPaymentExecutor.execute(paymentConfirmCommand))
                    .thenReturn(Mono.just(failureResult));

            PaymentConfirmationResult result = paymentConfirmService
                    .confirm(paymentConfirmCommand)
                    .block();

            // then
            verifyPaymentResult(result, PaymentStatus.FAILURE, false, true, false);
        }

        @Test
        @DisplayName("결제 확인에 알 수 없는 오류가 있을 때 결제 상태를 UNKNOWN으로 업데이트해야 함")
        void testUnknownErrorPaymentConfirmation() {
            // given
            PaymentFailure failure = new PaymentFailure("test code", "test message");
            PaymentExecutionResult unknownResult = createExecutionResult(
                    false,  // isSuccess
                    false,  // isFailure
                    true,   // isUnknown
                    failure
            );

            // when
            when(mockPaymentExecutor.execute(paymentConfirmCommand))
                    .thenReturn(Mono.just(unknownResult));

            PaymentConfirmationResult result = paymentConfirmService
                    .confirm(paymentConfirmCommand)
                    .block();

            // then
            verifyPaymentResult(result, PaymentStatus.UNKNOWN, false, false, true);
        }
    }

    @Nested
    @DisplayName("[PaymentConfirmService]예외 처리 테스트")
    class ExceptionHandlingTests {

        @Test
        @DisplayName("PSPConfirmationException을 적절히 처리해야 함")
        void testPSPConfirmationExceptionHandling() {
            // given
            PSPConfirmationException exception = PSPConfirmationException.builder()
                    .message(TossPaymentError.REJECT_ACCOUNT_PAYMENT.getDescription())
                    .isSuccess(false)
                    .isFailure(true)
                    .isUnknown(false)
                    .isRetryable(false)
                    .build();

            // when
            when(mockPaymentExecutor.execute(paymentConfirmCommand))
                    .thenReturn(Mono.error(exception));

            PaymentConfirmationResult result = paymentConfirmService
                    .confirm(paymentConfirmCommand)
                    .block();

            // then
            verifyPaymentResult(result, PaymentStatus.FAILURE, false, true, false);
        }

        @Test
        @DisplayName("PaymentValidationException을 적절히 처리해야 함")
        void testPaymentValidationExceptionHandling() {
            // given
            PaymentValidationException exception = new PaymentValidationException("결제 유효성 검사에서 실패했습니다.");

            // when
            when(mockPaymentExecutor.execute(paymentConfirmCommand))
                    .thenReturn(Mono.error(exception));

            PaymentConfirmationResult result = paymentConfirmService
                    .confirm(paymentConfirmCommand)
                    .block();

            // then
            verifyPaymentResult(result, PaymentStatus.FAILURE, false, true, false);
        }
    }

    private void verifyPaymentResult(PaymentConfirmationResult result, PaymentStatus expectedStatus, boolean checkSuccess, boolean checkFailure, boolean checkUnknown) {
        assertNotNull(result);
        PaymentEvent paymentEvent = paymentDatabaseHelper.getPayment(orderId);
        assertNotNull(paymentEvent);

        assertThat(result.getStatus()).isEqualTo(expectedStatus);

        if (checkSuccess) {
            assertTrue(paymentEvent.isSuccess());
        }

        if (checkFailure) {
            assertTrue(paymentEvent.isFailure());
        }

        if (checkUnknown) {
            assertTrue(paymentEvent.isUnknown());
        }
    }

    private PaymentExecutionResult createExecutionResult(
            boolean isSuccess,
            boolean isFailure,
            boolean isUnknown,
            PaymentFailure failure) {

        return new PaymentExecutionResult(
                paymentConfirmCommand.paymentKey(),
                paymentConfirmCommand.orderId(),
                createExtraDetails(),
                failure,
                isSuccess,
                isFailure,
                isUnknown
        );
    }

    private PaymentExecutionResult.PaymentExtraDetails createExtraDetails() {
        return new PaymentExecutionResult.PaymentExtraDetails(
                PaymentType.NORMAL,
                PaymentMethod.EASY_PAY,
                LocalDateTime.now(),
                "test_order_name",
                PSPConfirmationStatus.DONE,
                paymentConfirmCommand.amount(),
                "{}"
        );
    }
}
