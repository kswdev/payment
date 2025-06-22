package com.example.walletservice.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Wallet {
    private Long id;
    private Long userId;
    private int version;
    private BigDecimal balance;
    private List<WalletTransaction> walletTransactions = new ArrayList<>();

    public Wallet(Long id, Long userId, int version, BigDecimal balance) {
        this.id = id;
        this.userId = userId;
        this.version = version;
        this.balance = balance;
    }

    public Wallet calculateBalanceWith(List<? extends Item> items) {
        BigDecimal sumOfSoldPaymentValue = new BigDecimal(items.stream()
                .mapToLong(Item::getAmount)
                .sum());

        BigDecimal updatedBalance = getBalance().add(sumOfSoldPaymentValue);

        List<WalletTransaction> walletTransactions = items.stream()
                .map(item -> new WalletTransaction(
                        this.getId(),
                        item.getAmount(),
                        TransactionType.CREDIT,
                        item.getReferencesId(),
                        item.getReferenceType(),
                        item.getOrderId()
                ))
                .toList();

        return new Wallet(this.getId(), this.getUserId(), this.getVersion(), updatedBalance, walletTransactions);
    }
}
