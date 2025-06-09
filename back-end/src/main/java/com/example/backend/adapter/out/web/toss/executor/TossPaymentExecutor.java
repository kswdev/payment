package com.example.backend.adapter.out.web.toss.executor;

import com.example.backend.adapter.out.web.toss.response.TossPaymentConfirmationResponse;
import com.example.backend.application.command.PaymentConfirmCommand;
import com.example.backend.domain.PaymentExecutionResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class TossPaymentExecutor implements PaymentExecutor {

    private final WebClient tossPaymentWebClient;
    private final static String URL = "/v1/payments/confirm";

    @Override
    public Mono<PaymentExecutionResult>  execute(PaymentConfirmCommand command) {
        return tossPaymentWebClient.post()
                .uri(URL)
                .header("Idempotency-key", command.orderId())
                .bodyValue(String.format("{\"paymentKey\":\"%s\",\"orderId\":\"%s\",\"amount\":\"%s\"}", command.paymentKey(), command.orderId(), command.orderId()))
                .retrieve()
                .bodyToMono(TossPaymentConfirmationResponse.class)
                .map(PaymentExecutionResult::fromTossPaymentConfirmResponse);
    }
}
