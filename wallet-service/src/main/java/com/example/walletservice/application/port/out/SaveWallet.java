package com.example.walletservice.application.port.out;

import com.example.walletservice.domain.Wallet;

import java.util.List;

public interface SaveWallet {

    void saveWallets(List<Wallet> wallets);
}
