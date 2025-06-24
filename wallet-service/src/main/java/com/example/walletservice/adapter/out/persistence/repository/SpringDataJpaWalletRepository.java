package com.example.walletservice.adapter.out.persistence.repository;

import com.example.walletservice.adapter.out.persistence.entity.JpaWalletEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface SpringDataJpaWalletRepository extends JpaRepository<JpaWalletEntity, Long> {
    Set<JpaWalletEntity> findByUserIdIn(Set<Long> sellerIds);
}
