package org.frias.avalon.domain.credit.infrastructure.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "credit_account",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"clientId", "outletId"})
        })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreditAccountEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long clientId;

    @Column(nullable = false)
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
}
