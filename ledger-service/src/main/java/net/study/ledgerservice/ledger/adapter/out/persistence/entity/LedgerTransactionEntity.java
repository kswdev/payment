package net.study.ledgerservice.ledger.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;

@Entity
@Table(name = "ledger_transaction")
@Getter
public class LedgerTransactionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String description;

    @Column(name = "reference_id")
    private Long referenceId;

    @Column(name = "reference_type")
    private String referenceType;

    @Column(name = "order_id")
    private String orderId;

    @Column(name = "idempotency_key")
    private String idempotencyKey;

    @Builder
    public LedgerTransactionEntity(String description, Long referenceId, String referenceType, String orderId, String idempotencyKey) {
        this.description = description;
        this.referenceId = referenceId;
        this.referenceType = referenceType;
        this.orderId = orderId;
        this.idempotencyKey = idempotencyKey;
    }
}
