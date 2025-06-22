package com.example.walletservice.adapter.out.persistence.repository;

import com.example.walletservice.domain.Wallet;

import java.util.List;
import java.util.Set;

public interface WalletRepository {

    Set<Wallet> getWallets(Set<Long> sellerIds);

    void save(List<Wallet> wallets);
}
