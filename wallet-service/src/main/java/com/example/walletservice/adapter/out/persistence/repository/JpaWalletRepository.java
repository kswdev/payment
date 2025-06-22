package com.example.walletservice.adapter.out.persistence.repository;

import com.example.walletservice.adapter.out.persistence.entity.JpaWalletEntity;
import com.example.walletservice.domain.Wallet;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class JpaWalletRepository implements WalletRepository {

    private final SpringDataJpaWalletRepository springDataJpaWalletRepository;
    private final WalletTransactionRepository walletTransactionRepository;

    @Override
    public Set<Wallet> getWallets(Set<Long> sellerIds) {
        return springDataJpaWalletRepository.findBySellerIdIn(sellerIds).stream()
                .map(JpaWalletRepository::mapToWallet)
                .collect(Collectors.toSet());
    }

    @Override
    @Transactional
    public void save(List<Wallet> wallets) {
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
    }

    public interface SpringDataJpaWalletRepository extends JpaRepository<JpaWalletEntity, Long> {
        Set<JpaWalletEntity> findBySellerIdIn(Set<Long> sellerIds);
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
