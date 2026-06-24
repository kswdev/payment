package net.study.ledgerservice.ledger.application.port.out;

import net.study.ledgerservice.ledger.domain.PaymentOrder;

import java.util.List;

public interface LoadPaymentOrderPort {

    List<PaymentOrder> loadPaymentOrder(String orderId);
}
