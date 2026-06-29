package com.example.backend.adapter.in.stream;

import com.example.backend.application.port.in.PaymentCompleteUseCase;
import com.example.backend.common.StreamAdapter;
import com.example.backend.domain.LedgerEventMessage;
import com.example.backend.domain.WalletEventMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.kafka.receiver.ReceiverOffset;

import java.util.function.Function;

@Configuration
@StreamAdapter
@RequiredArgsConstructor
public class LedgerEventMessageHandler {

    private final PaymentCompleteUseCase paymentCompleteUseCase;

    @Bean
    public Function<Flux<Message<LedgerEventMessage>>, Mono<Void>> ledger() {
        return ledgerEventMessages -> ledgerEventMessages
                .flatMap(message ->
                        paymentCompleteUseCase
                                .completePayment(message.getPayload())
                                .then(Mono.defer(() -> message.getHeaders().get(KafkaHeaders.ACKNOWLEDGMENT, ReceiverOffset.class).commit()))
                )
                .then();
    }
}
