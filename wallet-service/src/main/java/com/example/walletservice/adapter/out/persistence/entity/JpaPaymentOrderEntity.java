package com.example.walletservice.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "payment_orders")
@Getter @Setter
public class JpaPaymentOrderEntity {

    @Id
    private Long id;

    @Column(name = "seller_id")
    private Long sellerId;

    private Long amount;

    @Column(name = "order_id")
    private String orderId;
}
