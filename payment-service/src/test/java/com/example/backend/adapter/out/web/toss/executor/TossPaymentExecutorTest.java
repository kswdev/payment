package com.example.backend.adapter.out.web.toss.executor;

import com.example.backend.adapter.out.web.toss.exception.PSPConfirmationException;
import com.example.backend.adapter.out.web.toss.exception.TossPaymentError;
import com.example.backend.application.command.PaymentConfirmCommand;
import com.example.backend.application.test.PSPTestWebClientConfiguration;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.util.Pair;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@Import(PSPTestWebClientConfiguration.class)
@Tag("TooLongTime")
class TossPaymentExecutorTest {

    private final PSPTestWebClientConfiguration pspTestWebClientConfiguration;

    TossPaymentExecutorTest(
            @Autowired PSPTestWebClientConfiguration pspTestWebClientConfiguration
    ) {
        this.pspTestWebClientConfiguration = pspTestWebClientConfiguration;
    }

    @Test
    void should_handle_correctly_various_tossPayment_error_scenarios() {
        generateErrorScenarios().forEach(errorScenario -> {
            PaymentConfirmCommand command = new PaymentConfirmCommand(
                    UUID.randomUUID().toString(),
                    UUID.randomUUID().toString(),
                    10000L
            );

            TossPaymentExecutor tossPaymentExecutor = new TossPaymentExecutor(
                    pspTestWebClientConfiguration.createTossPaymentWebClient(
                            Pair.of("TossPayments-Test-Code", errorScenario.errorCode)
                    )
            );

            tossPaymentExecutor.URL="/v1/payments/key-in";

            try {
                tossPaymentExecutor.execute(command).block();
            } catch (PSPConfirmationException e) {
                assertThat(e.isSuccess()).isEqualTo(errorScenario.isSuccess);
                assertThat(e.isFailure()).isEqualTo(errorScenario.isFailure);
                assertThat(e.isUnknown()).isEqualTo(errorScenario.isUnknown);
            }
        });
    }

    private static List<ErrorScenario> generateErrorScenarios() {
        return Arrays.stream(TossPaymentError.values())
                .map(tossPaymentError -> new ErrorScenario(
                        tossPaymentError.name(),
                        tossPaymentError.isSuccess(),
                        tossPaymentError.isUnknown(),
                        tossPaymentError.isFailure()
                )).toList();
    }

    private static class ErrorScenario {
        String errorCode;
        boolean isSuccess;
        boolean isUnknown;
        boolean isFailure;

        ErrorScenario(String errorCode, boolean isSuccess, boolean isUnknown, boolean isFailure) {
            this.errorCode = errorCode;
            this.isSuccess = isSuccess;
            this.isUnknown = isUnknown;
            this.isFailure = isFailure;
        }
    }
}