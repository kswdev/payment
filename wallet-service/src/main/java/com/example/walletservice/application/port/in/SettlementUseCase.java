package com.example.walletservice.application.port.in;

import com.example.walletservice.domain.PaymentEventMessage;
import com.example.walletservice.domain.WalletEventMessage;

public interface  SettlementUseCase {

     WalletEventMessage processSettlement(PaymentEventMessage paymentEventMessage);
}
