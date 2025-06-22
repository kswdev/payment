package com.example.walletservice.domain;

import lombok.Getter;

@Getter
public class WalletTransaction {
    private Long walletId;
    private Long amount;
    private TransactionType transactionType;
    private Long referencesId;
    private Item.ReferenceType referenceType;
    private String orderId;


    public WalletTransaction(Long walletId, Long amount, TransactionType transactionType, Long referencesId, Item.ReferenceType referenceType, String orderId) {
        this.walletId = walletId;
        this.amount = amount;
        this.transactionType = transactionType;
        this.referencesId = referencesId;
        this.referenceType = referenceType;
        this.orderId = orderId;
    }
}
