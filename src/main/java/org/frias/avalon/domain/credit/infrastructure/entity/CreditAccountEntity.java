package org.frias.avalon.domain.credit.infrastructure.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "credit_account",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_credit_account_client_outlet", columnNames = {"client_id", "outlet_id"})
        },
        indexes = {
                @Index(name = "idx_credit_account_outlet", columnList = "outlet_id"),
                @Index(name = "idx_credit_account_client_outlet", columnList = "client_id, outlet_id")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreditAccountEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "client_id", nullable = false)
    private Long clientId;

    @Column(name = "outlet_id", nullable = false)
    private Long outletId;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal creditLimit;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal currentDebt;

    @Column(nullable = false)
    private Long statusId;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Version
    private Long version;
}
