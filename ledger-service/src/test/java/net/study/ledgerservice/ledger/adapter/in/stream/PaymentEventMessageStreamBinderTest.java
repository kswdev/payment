package net.study.ledgerservice.ledger.adapter.in.stream;

import net.study.ledgerservice.ledger.application.port.in.DoubleLedgerEntryRecordUseCase;
import net.study.ledgerservice.ledger.domain.PaymentEventMessage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.binder.test.EnableTestBinder;
import org.springframework.cloud.stream.binder.test.InputDestination;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Map;

import static org.mockito.Mockito.verify;

@SpringBootTest
@EnableTestBinder
@ActiveProfiles("test")
public class PaymentEventMessageStreamBinderTest {

    @Autowired
    InputDestination inputDestination;

    @MockitoBean
    DoubleLedgerEntryRecordUseCase doubleLedger;

    @Test
    void recordDoubleLedgerEntry_when_consume_message() {
        //given
        PaymentEventMessage payload = new PaymentEventMessage(
                PaymentEventMessage.Type.PAYMENT_CONFIRMATION_SUCCESS,
                Map.of("orderId", "order-001"),
                Map.of("partitionKey", 2)
        );
        Message<PaymentEventMessage> message = MessageBuilder.withPayload(payload).build();

        //when
        inputDestination.send(message, "payment");

        //then
        verify(doubleLedger).recordDoubleLedgerEntry(payload);
    }
}
