package com.example.walletservice.adapter.out.persistence.repository;

import com.example.walletservice.domain.PaymentOrder;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentOrderRepository {
    List<PaymentOrder> getPaymentOrders(String orderId);
}
