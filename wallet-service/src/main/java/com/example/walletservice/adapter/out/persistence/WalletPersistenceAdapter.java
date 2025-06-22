package com.example.walletservice.adapter.out.persistence;

import com.example.walletservice.adapter.out.persistence.repository.WalletRepository;
import com.example.walletservice.adapter.out.persistence.repository.WalletTransactionRepository;
import com.example.walletservice.application.port.out.DuplicateMessageFilter;
import com.example.walletservice.application.port.out.LoadWallet;
import com.example.walletservice.application.port.out.SaveWallet;
import com.example.walletservice.common.PersistenceAdapter;
import com.example.walletservice.domain.PaymentEventMessage;
import com.example.walletservice.domain.Wallet;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
@PersistenceAdapter
@RequiredArgsConstructor
public class WalletPersistenceAdapter implements
        DuplicateMessageFilter,
        LoadWallet,
        SaveWallet
{

    private final WalletTransactionRepository walletTransactionRepository;
    private final WalletRepository walletRepository;

    @Override
    public Boolean isAlreadyProcess(PaymentEventMessage paymentEventMessage) {
        return walletTransactionRepository.isExist(paymentEventMessage);
    }

    @Override
    public Set<Wallet> getWallets(Set<Long> sellerIds) {
        return walletRepository.getWallets(sellerIds);
    }

    @Override
    public void saveWallets(List<Wallet> wallets) {
        walletRepository.save(wallets);
    }
}
