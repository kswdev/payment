package com.example.walletservice.application.service;

import com.example.walletservice.application.port.in.SettlementUseCase;
import com.example.walletservice.application.port.out.DuplicateMessageFilter;
import com.example.walletservice.application.port.out.LoadPaymentOrder;
import com.example.walletservice.application.port.out.LoadWallet;
import com.example.walletservice.application.port.out.SaveWallet;
import com.example.walletservice.common.UseCase;
import com.example.walletservice.domain.PaymentEventMessage;
import com.example.walletservice.domain.PaymentOrder;
import com.example.walletservice.domain.Wallet;
import com.example.walletservice.domain.WalletEventMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@UseCase
@Service
@RequiredArgsConstructor
public class SettlementService implements SettlementUseCase {

    private final DuplicateMessageFilter duplicateMessageFilter;
    private final LoadPaymentOrder loadPaymentOrder;
    private final LoadWallet loadWallet;
    private final SaveWallet saveWallet;

    @Override
    public WalletEventMessage processSettlement(PaymentEventMessage paymentEventMessage) {
        if (duplicateMessageFilter.isAlreadyProcess(paymentEventMessage)) {
            return createWalletEventMessage(paymentEventMessage);
        }

        List<PaymentOrder> paymentOrders = loadPaymentOrder.getPaymentOrders(paymentEventMessage.getOrderId());

        Map<Long, List<PaymentOrder>> paymentOrdersBySellerId = paymentOrders.stream()
                .collect(Collectors.groupingBy(PaymentOrder::getSellerId));

        List<Wallet> updatedWallets = getUpdatedWallets(paymentOrdersBySellerId);

        saveWallet.saveWallets(updatedWallets);

        return createWalletEventMessage(paymentEventMessage);
    }

    private List<Wallet> getUpdatedWallets(Map<Long, List<PaymentOrder>> paymentOrdersBySellerId) {
        Set<Long> keys = paymentOrdersBySellerId.keySet();
        Set<Wallet> wallets = loadWallet.getWallets(keys);

        return wallets.stream()
                .map(wallet -> wallet.calculateBalanceWith(paymentOrdersBySellerId.get(wallet.getUserId())))
                .toList();
    }

    private static WalletEventMessage createWalletEventMessage(PaymentEventMessage paymentEventMessage) {
        return new WalletEventMessage(
                WalletEventMessage.Type.SUCCESS,
                Map.of("orderId", paymentEventMessage.getOrderId())
        );
    }
}
