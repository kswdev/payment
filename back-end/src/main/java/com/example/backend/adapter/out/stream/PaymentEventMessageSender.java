package com.example.backend.adapter.out.stream;

import com.example.backend.adapter.out.persistence.repository.PaymentOutboxRepository;
import com.example.backend.application.port.out.DispatchEventMessagePort;
import com.example.backend.common.StreamAdapter;
import com.example.backend.domain.PaymentEventMessage;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.FluxMessageChannel;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.publisher.Sinks.Many;
import reactor.core.scheduler.Schedulers;
import reactor.kafka.sender.SenderResult;

import java.util.function.Supplier;

import static com.example.backend.domain.PaymentEventMessage.Type.PAYMENT_CONFIRMATION_SUCCESS;
import static org.springframework.integration.IntegrationMessageHeaderAccessor.CORRELATION_ID;

@Slf4j
@Configuration
@StreamAdapter
@RequiredArgsConstructor
public class PaymentEventMessageSender implements DispatchEventMessagePort {

    private final Many<Message<PaymentEventMessage>> sender = Sinks.many().unicast().onBackpressureBuffer();
    private final Many<SenderResult<String>> sendResult = Sinks.many().unicast().onBackpressureBuffer();

    private final PaymentOutboxRepository paymentOutboxRepository;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public Mono<Void> dispatchAfterCommit(PaymentEventMessage message) {
        return dispatch(message);
    }

    @Bean
    public Supplier<Flux<Message<PaymentEventMessage>>> send() {
        return () -> sender.asFlux()
                .onErrorContinue((err, __) -> {
                    log.error("Error occurred while sending message to stream", err);
                });
    }

    @Bean(name = "payment-result")
    public FluxMessageChannel sendResultChannel() {
        return new FluxMessageChannel();
    }

    @ServiceActivator(inputChannel = "payment-result")
    public void receiveSendResult(SenderResult<String> results) {
        if (results.exception() != null) {
            log.error("Error occurred while sending message to stream", results.exception());
        }

        sendResult.emitNext(results, Sinks.EmitFailureHandler.FAIL_FAST);
    }

    @PostConstruct
    public void handleSendResult() {
        sendResult.asFlux()
                .flatMap(sendResult -> {
                    if (sendResult.recordMetadata() != null)
                        return paymentOutboxRepository.markMessageAsSent(sendResult.correlationMetadata(), PAYMENT_CONFIRMATION_SUCCESS);
                    else
                        return paymentOutboxRepository.markMessageAsFailure(sendResult.correlationMetadata(), PAYMENT_CONFIRMATION_SUCCESS);
                })
                .onErrorContinue((err, __) -> {
                    log.error("Error occurred while marking message as sent or failure", err);
                })
                .subscribeOn(Schedulers.newSingle("payment-result-subscriber"))
                .subscribe();
    }

    protected Mono<Void> dispatch(PaymentEventMessage message) {
        Message<PaymentEventMessage> eventMessage = createEventMessage(message);
        sender.emitNext(eventMessage, Sinks.EmitFailureHandler.FAIL_FAST);
        return Mono.empty();
    }

    private static Message<PaymentEventMessage> createEventMessage(PaymentEventMessage message) {
        return MessageBuilder
                .withPayload(message)
                .setHeader(CORRELATION_ID, message.getPayload().get("orderId"))
                .setHeader(KafkaHeaders.PARTITION, message.getMetadata().get("partitionKey"))
                .build();
    }

}
