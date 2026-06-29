package com.example.backend.adapter.in.stream;

import com.example.backend.application.port.in.PaymentCompleteUseCase;
import com.example.backend.domain.LedgerEventMessage;
import com.example.backend.domain.WalletEventMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.binder.test.InputDestination;
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import reactor.core.publisher.Mono;
import reactor.kafka.receiver.ReceiverOffset;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

/**
 * Spring Cloud Stream 바인딩 통합 테스트
 *
 * InputDestination으로 메시지를 발행하면 test binder가 이를 수신하여
 * 해당 바인딩 함수(ledger / wallet)로 라우팅하는 전체 흐름을 검증.
 *
 * 바인딩 설정 (application.yaml):
 *   - ledger-in-0  destination: ledger  →  LedgerEventMessageHandler#ledger()
 *   - wallet-in-0  destination: wallet  →  WalletEventMessageHandler#wallet()
 */
@SpringBootTest(properties = {
        "spring.cloud.function.definition=ledger;wallet;send"
})
@Import(TestChannelBinderConfiguration.class)
@DisplayName("Stream 바인딩 통합 테스트")
class EventMessageHandlerBindingTest {

    @Autowired
    private InputDestination inputDestination;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PaymentCompleteUseCase paymentCompleteUseCase;

    // ─── Ledger 바인딩 ──────────────────────────────────────────

    @Test
    @DisplayName("[ledger] ledger 토픽에 발행된 원장 이벤트를 수신하여 결제 완료 처리한다")
    void should_commit_offset_when_ledger_event_is_published_to_ledger_topic() {
        // given
        ReceiverOffset receiverOffset = mock(ReceiverOffset.class);

        given(paymentCompleteUseCase.completePayment(any(LedgerEventMessage.class)))
                .willReturn(Mono.empty());
        given(receiverOffset.commit())
                .willReturn(Mono.empty());

        LedgerEventMessage eventMessage = new LedgerEventMessage(LedgerEventMessage.Type.SUCCESS);
        Message<LedgerEventMessage> payload = buildMessage(eventMessage, receiverOffset);

        // when - ledger 토픽으로 메시지 발행
        extracted(payload);

        // then - 핸들러가 수신하여 처리할 때까지 대기
        verify(paymentCompleteUseCase).completePayment(any(LedgerEventMessage.class));
        verify(receiverOffset).commit();
    }

    private void extracted(Message<LedgerEventMessage> payload) {
        inputDestination.send(payload, "ledger");
    }

    @Test
    @DisplayName("[ledger] ledger 토픽에 여러 메시지 발행 시 모두 순차적으로 처리한다")
    void should_commit_offset_for_each_message_when_multiple_ledger_events_are_published() throws Exception {
        // given
        int messageCount = 3;
        ReceiverOffset receiverOffset = mock(ReceiverOffset.class);

        given(paymentCompleteUseCase.completePayment(any(LedgerEventMessage.class)))
                .willReturn(Mono.empty());
        given(receiverOffset.commit())
                .willReturn(Mono.empty());

        LedgerEventMessage eventMessage = new LedgerEventMessage(LedgerEventMessage.Type.SUCCESS);
        Message<LedgerEventMessage> payload = buildMessage(eventMessage, receiverOffset);

        // when - 3개 메시지 발행
        for (int i = 0; i < messageCount; i++) {
            inputDestination.send(payload, "ledger");
        }

        // then
        verify(paymentCompleteUseCase, times(messageCount)).completePayment(any(LedgerEventMessage.class));
        verify(receiverOffset, times(messageCount)).commit();
    }

    // ─── Wallet 바인딩 ──────────────────────────────────────────

    @Test
    @DisplayName("[wallet] wallet 토픽에 발행된 정산 이벤트를 수신하여 결제 완료 처리한다")
    void should_commit_offset_when_wallet_event_is_published_to_wallet_topic() throws Exception {
        // given
        ReceiverOffset receiverOffset = mock(ReceiverOffset.class);

        given(paymentCompleteUseCase.completePayment(any(WalletEventMessage.class)))
                .willReturn(Mono.empty());
        given(receiverOffset.commit())
                .willReturn(Mono.empty());

        WalletEventMessage eventMessage = new WalletEventMessage(WalletEventMessage.Type.SUCCESS);
        Message<WalletEventMessage> payload = buildMessage(eventMessage, receiverOffset);

        // when - wallet 토픽으로 메시지 발행
        inputDestination.send(payload, "wallet");

        // then
        verify(paymentCompleteUseCase).completePayment(any(WalletEventMessage.class));
        verify(receiverOffset).commit();
    }

    @Test
    @DisplayName("[wallet] wallet 토픽에 여러 메시지 발행 시 모두 순차적으로 처리한다")
    void should_commit_offset_for_each_message_when_multiple_wallet_events_are_published() throws Exception {
        // given
        int messageCount = 3;
        ReceiverOffset receiverOffset = mock(ReceiverOffset.class);

        given(paymentCompleteUseCase.completePayment(any(WalletEventMessage.class)))
                .willReturn(Mono.empty());
        given(receiverOffset.commit())
                .willReturn(Mono.empty());

        WalletEventMessage eventMessage = new WalletEventMessage(WalletEventMessage.Type.SUCCESS);
        Message<WalletEventMessage> payload = buildMessage(eventMessage, receiverOffset);

        // when - 3개 메시지 발행
        for (int i = 0; i < messageCount; i++) {
            inputDestination.send(payload, "wallet");
        }

        // then
        verify(paymentCompleteUseCase, times(messageCount)).completePayment(any(WalletEventMessage.class));
        verify(receiverOffset, times(messageCount)).commit();
    }

    @NonNull
    private<T> Message<T> buildMessage(T eventMessage, ReceiverOffset receiverOffset) {
        return MessageBuilder.withPayload(eventMessage)
                .setHeader(KafkaHeaders.ACKNOWLEDGMENT, receiverOffset)
                .build();
    }
}
