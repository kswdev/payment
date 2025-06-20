package com.example.backend.adapter.out.web.toss.executor;

import com.example.backend.adapter.out.web.toss.exception.PSPConfirmationException;
import com.example.backend.adapter.out.web.toss.exception.TossPaymentError;
import com.example.backend.adapter.out.web.toss.response.TossPaymentConfirmationResponse;
import com.example.backend.application.command.PaymentConfirmCommand;
import com.example.backend.domain.PaymentExecutionResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.handler.timeout.TimeoutException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.time.Duration.*;

@Component
@RequiredArgsConstructor
public class TossPaymentExecutor implements PaymentExecutor {

    private final WebClient tossPaymentWebClient;

    public String URL = "/v1/payments/confirm";

    @Override
    public Mono<PaymentExecutionResult>  execute(PaymentConfirmCommand command) {
        return tossPaymentWebClient.post()
                .uri(URL)
                .header("Idempotency-key", command.orderId())
                .bodyValue(PaymentConfirmCommandToJson(command))
                .retrieve()
                .onStatus(isErrorHttpStatus(), mapTossFailureToException())
                .bodyToMono(TossPaymentConfirmationResponse.class)
                .map(PaymentExecutionResult::fromTossPaymentConfirmResponse)
                .retryWhen(Retry.backoff(2, ofSeconds(1)).jitter(0.1)
                        .filter(isErrorRetryable())
                        .onRetryExhaustedThrow((__, retrySignal) -> retrySignal.failure())
                );
    }

    private static String PaymentConfirmCommandToJson(PaymentConfirmCommand command) {
        return """
                {
                    "paymentKey": "%s",
                    "orderId": "%s",
                    "amount": "%s"
                }
                """.formatted(command.paymentKey(),
                command.orderId(),
                command.amount()).replaceAll("\\s+", "");
    }

    private static Predicate<HttpStatusCode> isErrorHttpStatus() {
        return statusCode ->
                statusCode.is4xxClientError() || statusCode.is5xxServerError();
    }

    private static Function<ClientResponse, Mono<? extends Throwable>> mapTossFailureToException() {
        return response -> response
                .bodyToMono(TossPaymentConfirmationResponse.TossFailureResponse.class)
                .flatMap(failure -> createPSPException(getTossPaymentError(failure)));
    }

    private static Mono<? extends Throwable> createPSPException(TossPaymentError error) {
        return Mono.error(
                PSPConfirmationException.builder()
                        .errorCode(error.name())
                        .message(error.getDescription())
                        .isSuccess(error.isSuccess())
                        .isFailure(error.isFailure())
                        .isUnknown(error.isUnknown())
                        .isRetryable(error.isRetryableError())
                        .build()
        );
    }

    private static TossPaymentError getTossPaymentError(
            TossPaymentConfirmationResponse.TossFailureResponse failure
    ) {
        return TossPaymentError.get(failure.getCode());
    }

    private static Predicate<Throwable> isErrorRetryable() {
        return throwable ->
                isPSPRetryableError(throwable)
                || isTimeoutError(throwable);
    }

    private static boolean isPSPRetryableError(Throwable throwable) {
        return throwable instanceof PSPConfirmationException pspException
                && pspException.isRetryable();
    }

    private static boolean isTimeoutError(Throwable throwable) {
        return throwable instanceof TimeoutException;
    }

}
