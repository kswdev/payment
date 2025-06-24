package com.example.walletservice.application.service;

import com.example.walletservice.adapter.out.persistence.entity.JpaWalletEntity;
import com.example.walletservice.adapter.out.persistence.repository.SpringDataJpaWalletRepository;
import com.example.walletservice.adapter.out.persistence.repository.SpringDataJpaWalletTransactionRepository;
import com.example.walletservice.application.port.out.DuplicateMessageFilter;
import com.example.walletservice.application.port.out.LoadPaymentOrder;
import com.example.walletservice.application.port.out.LoadWallet;
import com.example.walletservice.application.port.out.SaveWallet;
import com.example.walletservice.domain.PaymentEventMessage;
import com.example.walletservice.domain.PaymentOrder;
import com.example.walletservice.domain.Wallet;
import com.example.walletservice.domain.WalletEventMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
class SettlementServiceTest {

    @Mock
    private LoadPaymentOrder loadPaymentOrder;

    private final LoadWallet loadWallet;
    private final SaveWallet saveWallet;
    private final DuplicateMessageFilter duplicateMessageFilter;
    private final SpringDataJpaWalletRepository springDataJpaWalletRepository;
    private final SpringDataJpaWalletTransactionRepository springDataJpaWalletTransactionRepository;

    private SettlementService settlementService;

    SettlementServiceTest(
            @Autowired LoadWallet loadWallet,
            @Autowired SaveWallet saveWallet,
            @Autowired DuplicateMessageFilter duplicateMessageFilter,
            @Autowired SpringDataJpaWalletRepository springDataJpaWalletRepository,
            @Autowired SpringDataJpaWalletTransactionRepository springDataJpaWalletTransactionRepository
    ) {
        this.loadWallet = loadWallet;
        this.saveWallet = saveWallet;
        this.duplicateMessageFilter = duplicateMessageFilter;
        this.springDataJpaWalletRepository = springDataJpaWalletRepository;
        this.springDataJpaWalletTransactionRepository = springDataJpaWalletTransactionRepository;
    }

    @BeforeEach
    void clear() {
        springDataJpaWalletRepository.deleteAll();
        springDataJpaWalletTransactionRepository.deleteAll();

        settlementService = new SettlementService(
                duplicateMessageFilter,
                loadPaymentOrder,
                loadWallet,
                saveWallet
        );
    }

    @Test
    void should_settlement_process_successfully() {
        PaymentEventMessage paymentEventMessage = new PaymentEventMessage(
                PaymentEventMessage.Type.PAYMENT_CONFIRMATION_SUCCESS,
                Map.of("orderId", UUID.randomUUID().toString()),
                Map.of("metadata", "metadata")
        );

        List<JpaWalletEntity> walletEntities = List.of(
                new JpaWalletEntity(1L, BigDecimal.ZERO, 0),
                new JpaWalletEntity(2L, BigDecimal.ZERO, 0)
        );

        springDataJpaWalletRepository.saveAll(walletEntities);

        given(loadPaymentOrder.getPaymentOrders(paymentEventMessage.getOrderId())).willReturn(
                List.of(
                        new PaymentOrder(1L, 1L, 3000L, paymentEventMessage.getOrderId()),
                        new PaymentOrder(2L, 2L, 4000L, paymentEventMessage.getOrderId())
                )
        );

        WalletEventMessage walletEventMessage = settlementService.processSettlement(paymentEventMessage);

        Set<Long> sellerIds = walletEntities.stream()
                .map(JpaWalletEntity::getUserId)
                .collect(Collectors.toSet());

        List<Wallet> wallets = loadWallet.getWallets(sellerIds).stream()
                .sorted(Comparator.comparing(Wallet::getUserId))
                .toList();

        assertThat(walletEventMessage.getPayload().get("orderId")).isEqualTo(paymentEventMessage.getOrderId());
        assertThat(walletEventMessage.getType()).isEqualTo(WalletEventMessage.Type.SUCCESS);
        assertThat(wallets.get(0).getBalance().intValue()).isEqualTo(3000);
        assertThat(wallets.get(1).getBalance().intValue()).isEqualTo(4000);
    }
}