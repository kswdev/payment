package com.example.backend.adapter.out.web.toss.executor;

import ch.qos.logback.core.joran.util.ParentTag_Tag_Class_Tuple;
import com.example.backend.adapter.out.web.toss.exception.PSPConfirmationException;
import com.example.backend.adapter.out.web.toss.exception.TossPaymentError;
import com.example.backend.adapter.out.web.toss.response.TossPaymentConfirmationResponse;
import com.example.backend.application.command.PaymentConfirmCommand;
import com.example.backend.domain.PaymentExecutionResult;
import io.netty.handler.timeout.TimeoutException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

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
                .bodyValue(String.format("{\"paymentKey\":\"%s\",\"orderId\":\"%s\",\"amount\":\"%s\"}", command.paymentKey(), command.orderId(), command.amount()))
                .retrieve()
                .onStatus(statusCode ->
                        statusCode.is4xxClientError() || statusCode.is5xxServerError(),
                        response -> response.bodyToMono(TossPaymentConfirmationResponse.TossFailureResponse.class)
                                                          .flatMap(failure -> {
                                                              TossPaymentError error = TossPaymentError.get(failure.getCode());
                                                              return Mono.error(PSPConfirmationException.builder()
                                                                      .errorCode(error.name())
                                                                      .message(error.getDescription())
                                                                              .isSuccess(error.isSuccess())
                                                                              .isFailure(error.isFailure())
                                                                              .isUnknown(error.isUnknown())
                                                                              .isRetryable(error.isRetryableError())
                                                                      .build()
                                                              );
                                                          })
                )
                .bodyToMono(TossPaymentConfirmationResponse.class)
                .map(PaymentExecutionResult::fromTossPaymentConfirmResponse)
                .retryWhen(Retry.backoff(2, ofSeconds(1)).jitter(0.1)
                        .filter(throwable -> (throwable instanceof PSPConfirmationException && ((PSPConfirmationException) throwable).isRetryable()) || throwable instanceof TimeoutException)
                        .onRetryExhaustedThrow((__, retrySignal) -> retrySignal.failure())
                );
    }
}
