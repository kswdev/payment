package com.example.walletservice.adapter.out.persistence.repository;

import com.example.walletservice.adapter.out.persistence.entity.JpaWalletTransactionEntity;
import com.example.walletservice.common.IdempotencyCreator;
import com.example.walletservice.domain.PaymentEventMessage;
import com.example.walletservice.domain.WalletTransaction;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class JpaWalletTransactionRepository implements WalletTransactionRepository {

    private final SpringDataJpaWalletTransactionRepository springDataJpaWalletTransactionRepository;

    @Override
    public Boolean isExist(PaymentEventMessage paymentEventMessage) {
        return springDataJpaWalletTransactionRepository.existsByOrderId(paymentEventMessage.getOrderId());
    }

    @Override
    public void save(List<WalletTransaction> walletTransactions) {
        springDataJpaWalletTransactionRepository.saveAll(
                walletTransactions.stream().map(this::mapToJpaWalletTransactionEntity).toList()
        );
    }

    private JpaWalletTransactionEntity mapToJpaWalletTransactionEntity(WalletTransaction walletTransaction) {
        return new JpaWalletTransactionEntity(
                walletTransaction.getWalletId(),
                new BigDecimal(walletTransaction.getAmount()),
                walletTransaction.getTransactionType(),
                walletTransaction.getOrderId(),
                walletTransaction.getReferenceType().name(),
                walletTransaction.getReferencesId(),
                IdempotencyCreator.createIdempotencyKey(walletTransaction)
        );
    }
}
