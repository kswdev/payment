package com.example.walletservice.adapter.in.stream;

import com.example.walletservice.application.port.in.SettlementUseCase;
import com.example.walletservice.common.StreamAdapter;
import com.example.walletservice.domain.PaymentEventMessage;
import com.example.walletservice.domain.WalletEventMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;

import java.util.function.Consumer;

@Configuration
@StreamAdapter
@RequiredArgsConstructor
public class PaymentEventMessageHandler {

    private final SettlementUseCase settlementUseCase;
    private final StreamBridge streamBridge;

    @Bean
    public Consumer<Message<PaymentEventMessage>> consume() {
        return message -> {
            WalletEventMessage walletEventMessage = settlementUseCase.processSettlement(message.getPayload());
            streamBridge.send("wallet", walletEventMessage);
        };
    }
}
