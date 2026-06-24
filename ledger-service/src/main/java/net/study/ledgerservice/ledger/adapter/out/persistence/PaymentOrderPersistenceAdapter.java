package net.study.ledgerservice.ledger.adapter.out.persistence;

import lombok.RequiredArgsConstructor;
import net.study.ledgerservice.common.PersistenceAdapter;
import net.study.ledgerservice.ledger.adapter.out.persistence.repository.JpaPaymentOrderRepository;
import net.study.ledgerservice.ledger.application.port.out.LoadPaymentOrderPort;
import net.study.ledgerservice.ledger.domain.PaymentOrder;

import java.util.List;

@RequiredArgsConstructor
@PersistenceAdapter
public class PaymentOrderPersistenceAdapter implements LoadPaymentOrderPort {

    private final JpaPaymentOrderRepository jpaPaymentOrderRepository;

    @Override
    public List<PaymentOrder> loadPaymentOrder(String orderId) {
        return jpaPaymentOrderRepository.findByOrderId(orderId).stream()
                .map(entity -> new PaymentOrder(entity.getId(), entity.getAmount(), entity.getOrderId()))
                .toList();
    }
}
