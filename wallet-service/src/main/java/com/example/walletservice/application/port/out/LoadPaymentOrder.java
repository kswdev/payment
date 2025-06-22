package com.example.walletservice.application.port.out;

import com.example.walletservice.domain.PaymentOrder;

import java.util.List;

public interface LoadPaymentOrder {

    List<PaymentOrder> getPaymentOrders(String orderId);
}
