package com.example.walletservice.adapter.out.persistence.repository;

import com.example.walletservice.adapter.out.persistence.entity.JpaWalletEntity;
import com.example.walletservice.domain.Item;
import com.example.walletservice.domain.Wallet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import static java.util.concurrent.Executors.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class JpaWalletRepositoryTest {

    @Autowired JpaWalletRepository jpaWalletRepository;
    @Autowired SpringDataJpaWalletRepository springDataJpaWalletRepository;
    @Autowired SpringDataJpaWalletTransactionRepository springDataJpaWalletTransactionRepository;

    @BeforeEach
    void clear() {
        springDataJpaWalletRepository.deleteAll();
        springDataJpaWalletTransactionRepository.deleteAll();
    }

    @Test
    void should_update_wallet_balance_successfully_when_updated_command_at_the_same_time() throws ExecutionException, InterruptedException {
        JpaWalletEntity entity1 = new JpaWalletEntity(1L, BigDecimal.ZERO, 0);
        JpaWalletEntity entity2 = new JpaWalletEntity(2L, BigDecimal.ZERO, 0);

        springDataJpaWalletRepository.saveAll(List.of(entity1, entity2));

        Wallet wallet1 = mapToWallet(entity1);
        Wallet wallet2 = mapToWallet(entity2);

        List<Item> item1 = List.of(new Item(1000L, UUID.randomUUID().toString(), 1L, Item.ReferenceType.PAYMENT_ORDER));
        List<Item> item2 = List.of(new Item(2000L, UUID.randomUUID().toString(), 2L, Item.ReferenceType.PAYMENT_ORDER));
        List<Item> item3 = List.of(new Item(3000L, UUID.randomUUID().toString(), 3L, Item.ReferenceType.PAYMENT_ORDER));

        Wallet updatedWallet1 = wallet1.calculateBalanceWith(item1);
        Wallet updatedWallet2 = wallet1.calculateBalanceWith(item2);
        Wallet updatedWallet3 = wallet1.calculateBalanceWith(item3);

        Wallet updatedWallet4 = wallet2.calculateBalanceWith(item1);
        Wallet updatedWallet5 = wallet2.calculateBalanceWith(item2);
        Wallet updatedWallet6 = wallet2.calculateBalanceWith(item3);

        ExecutorService executorService = newFixedThreadPool(3);

        executorService.submit(() -> jpaWalletRepository.save(List.of(updatedWallet1, updatedWallet4))).get();
        executorService.submit(() -> jpaWalletRepository.save(List.of(updatedWallet2, updatedWallet5))).get();
        executorService.submit(() -> jpaWalletRepository.save(List.of(updatedWallet3, updatedWallet6))).get();

        JpaWalletEntity retrievedWallet = springDataJpaWalletRepository.findById(wallet1.getId()).get();
        JpaWalletEntity retrievedWallet2 = springDataJpaWalletRepository.findById(wallet2.getId()).get();

        assertThat(retrievedWallet.getVersion()).isEqualTo(3);
        assertThat(retrievedWallet2.getVersion()).isEqualTo(3);
        assertThat(retrievedWallet.getBalance().intValue()).isEqualTo(6000);
        assertThat(retrievedWallet2.getBalance().intValue()).isEqualTo(6000);
    }

    private static Wallet mapToWallet(JpaWalletEntity entity) {
        return new Wallet(
                entity.getId(),
                entity.getUserId(),
                entity.getVersion(),
                entity.getBalance()
        );
    }
}