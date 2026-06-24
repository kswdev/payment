package net.study.ledgerservice.ledger.adapter.out.persistence.entity;

import jakarta.persistence.*;
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
}
