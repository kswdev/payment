package net.study.ledgerservice.ledger.adapter.in.stream;

import net.study.ledgerservice.ledger.application.port.in.DoubleLedgerEntryRecordUseCase;
import net.study.ledgerservice.ledger.domain.LedgerEventMessage;
import net.study.ledgerservice.ledger.domain.PaymentEventMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import java.util.Map;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PaymentEventMessageHandlerTest {

    private PaymentEventMessageHandler handler;

    @Mock
    DoubleLedgerEntryRecordUseCase doubleLedger;

    @Mock
    StreamBridge streamBridge;

    @BeforeEach
    void setUp() {
        handler = new PaymentEventMessageHandler(doubleLedger, streamBridge);
    }

    @Test
    @DisplayName("결제 승인 완료 메시지를 정상적으로 소비한다")
    void consume_paymentConfirmationSuccess() {
        // given
        PaymentEventMessage payload = new PaymentEventMessage(
                PaymentEventMessage.Type.PAYMENT_CONFIRMATION_SUCCESS,
                Map.of("orderId", "order-001"),
                Map.of("partitionKey", 2)
        );
        LedgerEventMessage expectedMessage = new LedgerEventMessage(
                LedgerEventMessage.Type.SUCCESS,
                Map.of("orderId", "order-001"),
                Map.of("partitionKey", 2)
        );

        Message<PaymentEventMessage> message = MessageBuilder
                .withPayload(payload)
                .build();

        Consumer<Message<PaymentEventMessage>> consumer = handler.consume();

        given(doubleLedger.recordDoubleLedgerEntry(payload)).willReturn(expectedMessage);

        // when
        consumer.accept(message);

        // then
        verify(doubleLedger).recordDoubleLedgerEntry(payload);
        verify(streamBridge).send("ledger", expectedMessage);
    }

    @Test
    @DisplayName("metadata가 null인 경우에도 예외 없이 처리된다")
    void consume_withNullFields() {
        // given
        PaymentEventMessage payload = new PaymentEventMessage(
                PaymentEventMessage.Type.PAYMENT_CONFIRMATION_SUCCESS,
                Map.of("orderId", "order-null-test"),
                null
        );
        Message<PaymentEventMessage> message = MessageBuilder.withPayload(payload).build();
        Consumer<Message<PaymentEventMessage>> consumer = handler.consume();

        // when & then
        assertThatCode(() -> consumer.accept(message))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("여러 메시지를 연속으로 소비해도 예외가 발생하지 않는다")
    void consume_multipleMessages() {
        // given
        Consumer<Message<PaymentEventMessage>> consumer = handler.consume();

        // when & then
        for (int i = 0; i < 5; i++) {
            PaymentEventMessage payload = new PaymentEventMessage(
                    PaymentEventMessage.Type.PAYMENT_CONFIRMATION_SUCCESS,
                    Map.of("orderId", "order-" + i),
                    Map.of("seq", i)
            );
            Message<PaymentEventMessage> message = MessageBuilder.withPayload(payload).build();

            assertThatCode(() -> consumer.accept(message))
                    .doesNotThrowAnyException();
        }
    }
}
