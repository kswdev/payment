package com.example.backend.adapter.in.stream;

import com.example.backend.application.port.in.PaymentCompleteUseCase;
import com.example.backend.domain.WalletEventMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.kafka.receiver.ReceiverOffset;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.function.Function;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("WalletEventMessageHandler 단위 테스트")
class WalletEventMessageHandlerTest {

    @Mock
    private PaymentCompleteUseCase paymentCompleteUseCase;

    @InjectMocks
    private WalletEventMessageHandler handler;

    private Message<WalletEventMessage> buildMessage(WalletEventMessage eventMessage, ReceiverOffset receiverOffset) {
        return MessageBuilder.withPayload(eventMessage)
                .setHeader(KafkaHeaders.ACKNOWLEDGMENT, receiverOffset)
                .build();
    }

    @Nested
    @DisplayName("정상 처리")
    class SuccessCase {

        @Test
        @DisplayName("정산 이벤트 수신 시 결제 완료 처리 후 오프셋을 커밋한다")
        void should_commit_offset_when_wallet_event_is_received() {
            // given
            WalletEventMessage eventMessage = new WalletEventMessage(WalletEventMessage.Type.SUCCESS);
            ReceiverOffset receiverOffset = mock(ReceiverOffset.class);

            given(paymentCompleteUseCase.completePayment(eventMessage)).willReturn(Mono.empty());
            given(receiverOffset.commit()).willReturn(Mono.empty());

            Message<WalletEventMessage> message = buildMessage(eventMessage, receiverOffset);

            Function<Flux<Message<WalletEventMessage>>, Mono<Void>> walletFn = handler.wallet();

            // when
            StepVerifier.create(walletFn.apply(Flux.just(message)))
                    .verifyComplete();

            // then
            verify(paymentCompleteUseCase).completePayment(eventMessage);
            verify(receiverOffset).commit();
        }

        @Test
        @DisplayName("여러 정산 이벤트 수신 시 모두 순차적으로 처리하고 각 오프셋을 커밋한다")
        void should_commit_offset_for_each_message_when_multiple_wallet_events_are_received() {

            // given
            WalletEventMessage event1 = new WalletEventMessage(WalletEventMessage.Type.SUCCESS);
            WalletEventMessage event2 = new WalletEventMessage(WalletEventMessage.Type.SUCCESS);
            ReceiverOffset offset1 = mock(ReceiverOffset.class);
            ReceiverOffset offset2 = mock(ReceiverOffset.class);

            given(paymentCompleteUseCase.completePayment(event1)).willReturn(Mono.empty());
            given(paymentCompleteUseCase.completePayment(event2)).willReturn(Mono.empty());
            given(offset1.commit()).willReturn(Mono.empty());
            given(offset2.commit()).willReturn(Mono.empty());

            List<Message<WalletEventMessage>> messages = List.of(
                    buildMessage(event1, offset1),
                    buildMessage(event2, offset2)
            );

            Function<Flux<Message<WalletEventMessage>>, Mono<Void>> walletFn = handler.wallet();

            // when
            StepVerifier.create(walletFn.apply(Flux.fromIterable(messages)))
                    .verifyComplete();

            // then
            verify(paymentCompleteUseCase).completePayment(event1);
            verify(paymentCompleteUseCase).completePayment(event2);
            verify(offset1).commit();
            verify(offset2).commit();
        }
    }

    @Nested
    @DisplayName("에러 처리")
    class ErrorCase {

        @Test
        @DisplayName("결제 완료 처리 실패 시 오프셋을 커밋하지 않고 에러를 전파한다")
        void should_propagate_error_without_committing_offset_when_payment_complete_fails() {

            //given
            WalletEventMessage eventMessage = new WalletEventMessage(WalletEventMessage.Type.SUCCESS);
            ReceiverOffset receiverOffset = mock(ReceiverOffset.class);
            RuntimeException expectedException = new RuntimeException("결제 완료 처리 실패");

            given(paymentCompleteUseCase.completePayment(eventMessage))
                    .willReturn(Mono.error(expectedException));

            Message<WalletEventMessage> message = buildMessage(eventMessage, receiverOffset);

            Function<Flux<Message<WalletEventMessage>>, Mono<Void>> walletFn = handler.wallet();

            //when
            StepVerifier.create(walletFn.apply(Flux.just(message)))
                    .expectError(RuntimeException.class)
                    .verify();

            //then
            verify(paymentCompleteUseCase).completePayment(eventMessage);
            verify(receiverOffset, never()).commit();
        }
    }
}
