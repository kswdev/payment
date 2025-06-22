package com.example.walletservice.adapter.out.persistence.repository;

import com.example.walletservice.adapter.out.persistence.entity.JpaPaymentOrderEntity;
import com.example.walletservice.domain.PaymentOrder;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class JpaPaymentOrderRepository implements PaymentOrderRepository {

    private final SpringDataJpaPaymentOrderRepository springDataJpaPaymentOrderRepository;

    @Override
    public List<PaymentOrder> getPaymentOrders(String orderId) {
        return springDataJpaPaymentOrderRepository.findByOrderId(orderId).stream()
                .map(JpaPaymentOrderRepository::mapToPaymentOrder)
                .toList();
    }

    public interface SpringDataJpaPaymentOrderRepository extends JpaRepository<JpaPaymentOrderEntity, Long> {
        List<JpaPaymentOrderEntity> findByOrderId(String orderId);
    }

    private static PaymentOrder mapToPaymentOrder(JpaPaymentOrderEntity entity) {
        return new PaymentOrder(entity.getId(), entity.getSellerId(), entity.getAmount(), entity.getOrderId());
    }
}
