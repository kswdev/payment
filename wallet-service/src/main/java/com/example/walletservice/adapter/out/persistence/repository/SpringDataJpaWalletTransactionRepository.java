package com.example.walletservice.adapter.out.persistence.repository;

import com.example.walletservice.adapter.out.persistence.entity.JpaWalletTransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpringDataJpaWalletTransactionRepository extends JpaRepository<JpaWalletTransactionEntity, Long> {
    Boolean existsByOrderId(String orderId);
}