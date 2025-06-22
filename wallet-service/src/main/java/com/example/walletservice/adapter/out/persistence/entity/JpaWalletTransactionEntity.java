package com.example.walletservice.adapter.out.persistence.entity;

import com.example.walletservice.domain.TransactionType;
import jakarta.persistence.*;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "wallet_transactions")
@NoArgsConstructor
public class JpaWalletTransactionEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "wallet_id")
    private Long walletId;

    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private TransactionType transactionType;

    @Column(name = "order_id")
    private String orderId;

    @Column(name = "reference_type")
    private String referenceType;

    @Column(name = "reference_id")
    private Long referenceId;

    @Column(name = "idempotency_key")
    private String idempotencyKey;

    public JpaWalletTransactionEntity(Long walletId, BigDecimal amount, TransactionType transactionType, String orderId, String referenceType, Long referenceId, String idempotencyKey) {
        this.walletId = walletId;
        this.amount = amount;
        this.transactionType = transactionType;
        this.orderId = orderId;
        this.referenceType = referenceType;
        this.referenceId = referenceId;
        this.idempotencyKey = idempotencyKey;
    }
}
