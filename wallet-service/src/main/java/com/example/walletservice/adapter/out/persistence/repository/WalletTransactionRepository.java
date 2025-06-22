package com.example.walletservice.adapter.out.persistence.repository;

import com.example.walletservice.domain.PaymentEventMessage;
import com.example.walletservice.domain.WalletTransaction;

import java.util.List;

public interface WalletTransactionRepository {

    Boolean isExist(PaymentEventMessage paymentEventMessage);
    void save(List<WalletTransaction> walletTransactions);
}
