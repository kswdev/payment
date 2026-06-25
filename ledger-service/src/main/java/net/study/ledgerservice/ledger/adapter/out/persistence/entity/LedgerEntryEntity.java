package net.study.ledgerservice.ledger.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import net.study.ledgerservice.ledger.domain.LedgerEntry;

import java.math.BigDecimal;

@Table(name = "ledger_entry")
@Entity
@Getter
public class LedgerEntryEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private BigDecimal amount;

    @Column(name = "account_id")
    private Long accountId;

    @ManyToOne(cascade = CascadeType.PERSIST)
    private LedgerTransactionEntity transaction;

    @Enumerated(EnumType.STRING)
    private LedgerEntry.LedgerEntryType type;


    @Builder
    public LedgerEntryEntity(BigDecimal amount, Long accountId, LedgerTransactionEntity transaction, LedgerEntry.LedgerEntryType type) {
        this.amount = amount;
        this.accountId = accountId;
        this.transaction = transaction;
        this.type = type;
    }
}
