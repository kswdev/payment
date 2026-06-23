package net.study.ledgerservice.ledger.adapter.in.stream;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.ledgerservice.common.StreamAdapter;
import net.study.ledgerservice.ledger.application.port.in.DoubleLedgerEntryRecordUseCase;
import net.study.ledgerservice.ledger.domain.LedgerEventMessage;
import net.study.ledgerservice.ledger.domain.PaymentEventMessage;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;

import java.util.function.Consumer;

@Slf4j
@StreamAdapter
@RequiredArgsConstructor
@Configuration
public class PaymentEventMessageHandler {

    private final DoubleLedgerEntryRecordUseCase doubleLedger;
    private final StreamBridge streamBridge;

    @Bean
    public Consumer<Message<PaymentEventMessage>> consume() {
        return message -> {
            LedgerEventMessage ledgerEventMessage = doubleLedger.recordDoubleLedgerEntry(message.getPayload());
            streamBridge.send("ledger", ledgerEventMessage);
        };
    }
}
