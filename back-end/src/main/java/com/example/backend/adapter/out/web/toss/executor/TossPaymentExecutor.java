package com.example.backend.adapter.out.web.toss.executor;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class TossPaymentExecutor {

    private final WebClient tossPaymentWebClient;
    private final static String URL = "/v1/payments/confirm";

    public Mono<String> execute(String paymentKey, String orderId, String amount) {
        return tossPaymentWebClient.post()
                .uri(URL)
                .bodyValue(String.format("{\"paymentKey\":\"%s\",\"orderId\":\"%s\",\"amount\":\"%s\"}", paymentKey, orderId, amount))
                .retrieve()
                .bodyToMono(String.class);
    }
}
