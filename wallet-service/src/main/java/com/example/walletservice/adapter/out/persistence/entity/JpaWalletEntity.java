package com.example.walletservice.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "wallets")
@Getter
@AllArgsConstructor @NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class JpaWalletEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    private BigDecimal balance;

    @Version
    private int version;

    public JpaWalletEntity(Long userId, BigDecimal balance, int version) {
        this.userId = userId;
        this.balance = balance;
        this.version = version;
    }

    public JpaWalletEntity addBalance(BigDecimal amount) {
        return new JpaWalletEntity(this.getId(), this.getUserId(), this.getBalance().add(amount), this.getVersion());
    }
}
