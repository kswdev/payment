package com.example.walletservice.adapter.out.persistence;

import com.example.walletservice.adapter.out.persistence.repository.JpaPaymentOrderRepository;
import com.example.walletservice.application.port.out.LoadPaymentOrder;
import com.example.walletservice.common.PersistenceAdapter;
import com.example.walletservice.domain.PaymentOrder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@PersistenceAdapter
@RequiredArgsConstructor
public class PaymentOrderPersistenceAdapter implements LoadPaymentOrder {

    private final JpaPaymentOrderRepository jpaPaymentOrderRepository;

    @Override
    public List<PaymentOrder> getPaymentOrders(String orderId) {
        return jpaPaymentOrderRepository.getPaymentOrders(orderId);
    }
}
