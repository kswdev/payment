package com.example.walletservice.adapter.out.persistence.repository;

import com.example.walletservice.adapter.out.persistence.entity.JpaWalletEntity;
import com.example.walletservice.domain.Wallet;
import com.example.walletservice.domain.WalletTransaction;
import lombok.RequiredArgsConstructor;
import org.springframework.data.util.Pair;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.ExhaustedRetryException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class JpaWalletRepository implements WalletRepository {

    private final SpringDataJpaWalletRepository springDataJpaWalletRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final TransactionTemplate transactionTemplate;

    @Override
    public Set<Wallet> getWallets(Set<Long> sellerIds) {
        return springDataJpaWalletRepository.findByUserIdIn(sellerIds).stream()
                .map(JpaWalletRepository::mapToWallet)
                .collect(Collectors.toSet());
    }

    @Override
    public void save(List<Wallet> wallets) {
        try {
            performSaveOperation(wallets);
        } catch (ObjectOptimisticLockingFailureException e) {
            retrySaveOperation(wallets, 3, 100);
        }
    }

    private void performSaveOperation(List<Wallet> wallets) {
        transactionTemplate.execute(__ -> {
            springDataJpaWalletRepository.saveAll(
                    wallets.stream()
                            .map(this::mapToJpaWalletEntity)
                            .toList()
            );

            walletTransactionRepository.save(
                    wallets.stream()
                            .map(Wallet::getWalletTransactions)
                            .flatMap(Collection::stream)
                            .toList()
            );
            return null;
        });
    }

    private void retrySaveOperation(List<Wallet> wallets, int maxRetryCount, long sleepTime) {
        int retryCount = 0;

        while (true) {
            try {
                performSaveOperation(wallets);
                break;
            } catch (ObjectOptimisticLockingFailureException e) {
                if (++retryCount > maxRetryCount) {
                    throw new ExhaustedRetryException("Retry count exceeded for wallet save operation");
                }
            }
        }
    }

    private void performSaveOperationWithRecent(List<Wallet> wallets) {
        Set<JpaWalletEntity> recentWallets = springDataJpaWalletRepository.findByIdIn(
                wallets.stream()
                        .map(Wallet::getId)
                        .collect(Collectors.toSet())
        );

        Map<Long, JpaWalletEntity> recentWalletById = recentWallets.stream()
                .collect(Collectors.toMap(
                        JpaWalletEntity::getId,
                        Function.identity()
                ));


        List<Pair<Wallet, JpaWalletEntity>> walletPairs = wallets.stream()
                .map(wallet -> Pair.of(wallet, recentWalletById.get(wallet.getId())))
                .toList();

        List<JpaWalletEntity> updateWallets = walletPairs.stream()
                .map(walletPair -> {
                    BigDecimal sumOfSoldPaymentValue = new BigDecimal(walletPair.getFirst().getWalletTransactions().stream()
                            .mapToLong(WalletTransaction::getAmount)
                            .sum());

                    return walletPair.getSecond().addBalance(sumOfSoldPaymentValue);
                }).toList();

        transactionTemplate.execute(__ -> {
            springDataJpaWalletRepository.saveAll(updateWallets);
            walletTransactionRepository.save(
                    wallets.stream()
                            .map(Wallet::getWalletTransactions)
                            .flatMap(Collection::stream)
                            .toList());
            return null;
        });
    }

    private void waitForNextRetry(int baseDelay) {
        long jitter = (long) (Math.random() * baseDelay);
        try {
            Thread.sleep(jitter);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while waiting for next retry", e);
        }
    }


    private static Wallet mapToWallet(JpaWalletEntity entity) {
        return new Wallet(
                entity.getId(),
                entity.getUserId(),
                entity.getVersion(),
                entity.getBalance()
        );
    }

    private JpaWalletEntity mapToJpaWalletEntity(Wallet wallet) {
        return new JpaWalletEntity(wallet.getId(), wallet.getUserId(), wallet.getBalance(), wallet.getVersion());
    }
}
