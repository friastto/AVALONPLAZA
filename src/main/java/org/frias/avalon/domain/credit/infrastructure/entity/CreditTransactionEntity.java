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
@Table(name = "credit_transaction",
        indexes = {
                @Index(name = "idx_credit_txn_account", columnList = "credit_account_id"),
                @Index(name = "idx_credit_txn_created_at", columnList = "created_at")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreditTransactionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "credit_account_id", nullable = false)
    private Long creditAccountId;

    private Long saleId;

    @Column(nullable = false)
    private String type; // PURCHASE, PAYMENT

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal previousDebt;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal newDebt;

    private String notes;

    @Column(nullable = false)
    private Long registeredBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
