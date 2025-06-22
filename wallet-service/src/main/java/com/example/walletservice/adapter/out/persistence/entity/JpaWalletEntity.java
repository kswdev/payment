package com.example.walletservice.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "wallets")
@Getter
@AllArgsConstructor @NoArgsConstructor
public class JpaWalletEntity {

    @Id
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    private BigDecimal balance;

    @Version
    private int version;

}
