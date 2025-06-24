package com.example.walletservice.adapter.out.persistence.repository;

import com.example.walletservice.adapter.out.persistence.entity.JpaPaymentOrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpringDataJpaPaymentOrderRepository extends JpaRepository<JpaPaymentOrderEntity, Long> {
    List<JpaPaymentOrderEntity> findByOrderId(String orderId);
}