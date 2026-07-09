package org.frias.avalon.domain.credit.infrastructure.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "credit_transaction")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreditTransactionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long creditAccountId;

    private Long saleId; // nullable

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

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
