package com.example.walletservice.application.port.out;

import com.example.walletservice.domain.Wallet;

import java.util.Set;

public interface LoadWallet {

    Set<Wallet> getWallets(Set<Long> sellerIds);
}
