package com.example.backend.adapter.out.stream;

import com.example.backend.domain.PaymentEventMessage;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.example.backend.domain.PaymentEventMessage.Type.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PaymentEventMessageSenderTest {

    private final PaymentEventMessageSender paymentEventMessageSender;

    PaymentEventMessageSenderTest(
            @Autowired PaymentEventMessageSender paymentEventMessageSender
    ) {
        this.paymentEventMessageSender = paymentEventMessageSender;
    }

    @Test
    @Tag("ExternalIntegration")
    void should_send_message_event_by_using_partitionKey() throws InterruptedException {
        List<PaymentEventMessage> paymentEventMessages = List.of(
                new PaymentEventMessage(
                        PAYMENT_CONFIRMATION_SUCCESS,
                        Map.of("orderId", UUID.randomUUID().toString()),
                        Map.of("partitionKey", 1)),
                new PaymentEventMessage(
                        PAYMENT_CONFIRMATION_SUCCESS,
                        Map.of("orderId", UUID.randomUUID().toString()),
                        Map.of("partitionKey", 2)),
                new PaymentEventMessage(
                        PAYMENT_CONFIRMATION_SUCCESS,
                        Map.of("orderId", UUID.randomUUID().toString()),
                        Map.of("partitionKey", 3)),
                new PaymentEventMessage(
                        PAYMENT_CONFIRMATION_SUCCESS,
                        Map.of("orderId", UUID.randomUUID().toString()),
                        Map.of("partitionKey", 4)),
                new PaymentEventMessage(
                        PAYMENT_CONFIRMATION_SUCCESS,
                        Map.of("orderId", UUID.randomUUID().toString()),
                        Map.of("partitionKey", 5))
        );

        paymentEventMessages.forEach(paymentEventMessageSender::dispatch);

        // 메시지 전송이 완료될 때까지 충분한 시간 대기
        try {
            Thread.sleep(2000); // 또는 더 정교한 대기 로직
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}