package com.example.backend.adaptor.out.web.executor;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class TossPaymentExecutor {

    private final WebClient tossPaymentWebClient;
    private final static String URL = "/v1/toss/confirm";

    public Mono<String> execute(String paymentKey, String orderId, String amount) {
        return tossPaymentWebClient.post()
                .uri(URL)
                .bodyValue(String.format("{\"paymentKey\":\"%s\",\"orderId\":\"%s\",\"amount\":\"%s\"}", paymentKey, orderId, amount))
                .retrieve()
                .bodyToMono(String.class);
    }
}
