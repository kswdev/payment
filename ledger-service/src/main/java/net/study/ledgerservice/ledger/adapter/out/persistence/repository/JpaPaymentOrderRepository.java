package net.study.ledgerservice.ledger.adapter.out.persistence.repository;

import net.study.ledgerservice.ledger.adapter.out.persistence.entity.PaymentOrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JpaPaymentOrderRepository extends JpaRepository<PaymentOrderEntity, Long> {

    List<PaymentOrderEntity> findByOrderId(String orderId);
}
