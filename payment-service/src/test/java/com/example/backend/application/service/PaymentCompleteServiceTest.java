package com.example.backend.application.service;

import com.example.backend.application.port.out.CompletePaymentPort;
import com.example.backend.application.port.out.LoadPaymentPort;
import com.example.backend.domain.LedgerEventMessage;
import com.example.backend.domain.PaymentEvent;
import com.example.backend.domain.PaymentOrder;
import com.example.backend.domain.WalletEventMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;


@ExtendWith(MockitoExtension.class)
class PaymentCompleteServiceTest {

    @Mock
    private LoadPaymentPort loadPaymentPort;

    @Mock
    private CompletePaymentPort completePaymentPort;

    @InjectMocks
    private PaymentCompleteService paymentCompleteService;

    @Nested
    @DisplayName("정상 처리")
    class SuccessCase {

        @Test
        @DisplayName("지갑 이벤트를 완료 처리한 후 원장 이벤트가 이미 완료되어 있으면 결제를 완료한다.")
        void should_complete_payment_when_wallet_event_is_received_and_ledger_event_has_already_been_processed() {
            // given
            String orderId = "order-1";
            WalletEventMessage walletEventMessage = walletEventMessage(WalletEventMessage.Type.SUCCESS, Map.of("orderId", orderId), Map.of());
            List<PaymentOrder> paymentOrders = List.of(
                    paymentOrder(orderId, true, false, false),
                    paymentOrder(orderId, true, false, false)
            );
            PaymentEvent paymentEvent = paymentEvent(orderId, paymentOrders);

            given(loadPaymentPort.getPayment(walletEventMessage.orderId()))
                    .willReturn(Mono.just(paymentEvent));

            given(completePaymentPort.complete(paymentEvent))
                    .willReturn(Mono.empty());

            Mono<Void> result = paymentCompleteService.completePayment(walletEventMessage);

            // when
            StepVerifier
                    .create(result)
                    .verifyComplete();

            // then
            ArgumentCaptor<PaymentEvent> paymentEventCaptor = ArgumentCaptor.forClass(PaymentEvent.class);
            verify(completePaymentPort).complete(paymentEventCaptor.capture());
            verify(loadPaymentPort).getPayment(walletEventMessage.orderId());

            PaymentEvent actual = paymentEventCaptor.getValue();
            assertThat(actual.isLedgerUpdateDone()).isTrue();
            assertThat(actual.isWalletUpdateDone()).isTrue();
            assertThat(actual.isPaymentDone()).isTrue();
        }

        @Test
        @DisplayName("지갑 이벤트를 완료 처리한 후 원장 이벤트가 완료되어 있지 않다면 결제를 완료하지 않는다.")
        void should_not_complete_payment_when_wallet_event_is_received_and_ledger_event_has_not_been_processed() {
            // given
            String orderId = "order-1";
            WalletEventMessage walletEventMessage = walletEventMessage(WalletEventMessage.Type.SUCCESS, Map.of("orderId", orderId), Map.of());
            List<PaymentOrder> paymentOrders = List.of(
                    paymentOrder(orderId, false, false, false),
                    paymentOrder(orderId, false, false, false)
            );
            PaymentEvent paymentEvent = paymentEvent(orderId, paymentOrders);

            given(loadPaymentPort.getPayment(walletEventMessage.orderId()))
                    .willReturn(Mono.just(paymentEvent));

            given(completePaymentPort.complete(paymentEvent))
                    .willReturn(Mono.empty());

            Mono<Void> result = paymentCompleteService.completePayment(walletEventMessage);

            // when
            StepVerifier
                    .create(result)
                    .verifyComplete();

            // then
            ArgumentCaptor<PaymentEvent> paymentEventCaptor = ArgumentCaptor.forClass(PaymentEvent.class);
            verify(completePaymentPort).complete(paymentEventCaptor.capture());
            verify(loadPaymentPort).getPayment(walletEventMessage.orderId());

            PaymentEvent actual = paymentEventCaptor.getValue();
            assertThat(actual.isLedgerUpdateDone()).isFalse();
            assertThat(actual.isWalletUpdateDone()).isTrue();
            assertThat(actual.isPaymentDone()).isFalse();
        }

        @Test
        @DisplayName("원장 이벤트를 완료 처리한 후 지갑 이벤트가 이미 완료되어 있으면 결제를 완료한다.")
        void should_complete_payment_when_ledger_event_is_received_and_wallet_event_has_already_been_processed() {
            // given
            String orderId = "order-1";
            LedgerEventMessage ledgerEventMessage = ledgerEventMessage(LedgerEventMessage.Type.SUCCESS, Map.of("orderId", orderId), Map.of());
            List<PaymentOrder> paymentOrders = List.of(
                    paymentOrder(orderId, false, true, false),
                    paymentOrder(orderId, false, true, false)
            );
            PaymentEvent paymentEvent = paymentEvent(orderId, paymentOrders);

            given(loadPaymentPort.getPayment(ledgerEventMessage.orderId()))
                    .willReturn(Mono.just(paymentEvent));

            given(completePaymentPort.complete(paymentEvent))
                    .willReturn(Mono.empty());

            Mono<Void> result = paymentCompleteService.completePayment(ledgerEventMessage);

            // when
            StepVerifier
                    .create(result)
                    .verifyComplete();

            // then
            ArgumentCaptor<PaymentEvent> paymentEventCaptor = ArgumentCaptor.forClass(PaymentEvent.class);
            verify(completePaymentPort).complete(paymentEventCaptor.capture());
            verify(loadPaymentPort).getPayment(ledgerEventMessage.orderId());

            PaymentEvent actual = paymentEventCaptor.getValue();
            assertThat(actual.isLedgerUpdateDone()).isTrue();
            assertThat(actual.isWalletUpdateDone()).isTrue();
            assertThat(actual.isPaymentDone()).isTrue();
        }

        @Test
        @DisplayName("원장 이벤트를 완료 처리한 후 지갑 이벤트가 완료되어 있지 않다면 결제를 완료하지 않는다.")
        void should_not_complete_payment_when_ledger_event_is_received_and_wallet_event_has_not_been_processed() {
            // given
            String orderId = "order-1";
            LedgerEventMessage ledgerEventMessage = ledgerEventMessage(LedgerEventMessage.Type.SUCCESS, Map.of("orderId", orderId), Map.of());
            List<PaymentOrder> paymentOrders = List.of(
                    paymentOrder(orderId, false, false, false),
                    paymentOrder(orderId, false, false, false)
            );
            PaymentEvent paymentEvent = paymentEvent(orderId, paymentOrders);

            given(loadPaymentPort.getPayment(ledgerEventMessage.orderId()))
                    .willReturn(Mono.just(paymentEvent));

            given(completePaymentPort.complete(paymentEvent))
                    .willReturn(Mono.empty());

            Mono<Void> result = paymentCompleteService.completePayment(ledgerEventMessage);

            // when
            StepVerifier
                    .create(result)
                    .verifyComplete();

            // then
            ArgumentCaptor<PaymentEvent> paymentEventCaptor = ArgumentCaptor.forClass(PaymentEvent.class);
            verify(completePaymentPort).complete(paymentEventCaptor.capture());
            verify(loadPaymentPort).getPayment(ledgerEventMessage.orderId());

            PaymentEvent actual = paymentEventCaptor.getValue();
            assertThat(actual.isLedgerUpdateDone()).isTrue();
            assertThat(actual.isWalletUpdateDone()).isFalse();
            assertThat(actual.isPaymentDone()).isFalse();
        }
    }

    private WalletEventMessage walletEventMessage(WalletEventMessage.Type type, Map<String, ?> payload, Map<String, ?> metadata) {
        return new WalletEventMessage(type, payload, metadata);
    }

    private LedgerEventMessage ledgerEventMessage(LedgerEventMessage.Type type, Map<String, ?> payload, Map<String, ?> metadata) {
        return new LedgerEventMessage(type, payload, metadata);
    }

    private PaymentEvent paymentEvent(String orderId, List<PaymentOrder> paymentOrders) {
        return PaymentEvent.builder()
                .orderId(orderId)
                .paymentOrders(paymentOrders)
                .build();
    }

    private PaymentOrder paymentOrder(String orderId, Boolean isLedgerUpdated, Boolean isWalletUpdated, Boolean isPaymentDone) {
        return PaymentOrder.builder()
                .orderId(orderId)
                .isLedgerUpdated(isLedgerUpdated)
                .isWalletUpdated(isWalletUpdated)
                .isPaymentDone(isPaymentDone)
                .build();
    }
}