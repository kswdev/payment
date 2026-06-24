package net.study.ledgerservice.ledger.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

@Getter
@Table(name = "payment_orders")
@Entity
public class PaymentOrderEntity {

    @Id
    private Long id;

    private Long amount;

    @Column(name = "order_id")
    private String orderId;
}
