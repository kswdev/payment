package com.example.walletservice.adapter.out.persistence.repository;

import com.example.walletservice.domain.PaymentOrder;

import java.util.List;

public interface PaymentOrderRepository {
    List<PaymentOrder> getPaymentOrders(String orderId);
}
