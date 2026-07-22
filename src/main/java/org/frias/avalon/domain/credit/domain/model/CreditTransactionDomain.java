package org.frias.avalon.domain.credit.domain.model;

import org.frias.avalon.core.exeptions.BusinessException;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Domain entity representing a transaction in the credit account (Libreta de Fiado).
 * It acts as an immutable audit trail of purchases and payments.
 */
public class CreditTransactionDomain {

    private Long id;
    private Long creditAccountId;
    private Long saleId;
    private String type; // "PURCHASE" or "PAYMENT"
    private BigDecimal amount;
    private BigDecimal previousDebt;
    private BigDecimal newDebt;
    private String notes;
    private Long registeredBy;
    private LocalDateTime createdAt;

    private CreditTransactionDomain() {}

    /**
     * Creates a new credit transaction record.
     */
    public static CreditTransactionDomain create(
            Long creditAccountId, Long saleId, String type, BigDecimal amount,
            BigDecimal previousDebt, BigDecimal newDebt, String notes, Long registeredBy) {

        if (creditAccountId == null) throw new BusinessException("Credit account ID is required");
        if (type == null || (!type.equals("PURCHASE") && !type.equals("PAYMENT") && !type.equals("RETURN_CREDIT"))) {
            throw new BusinessException("Invalid transaction type: " + type);
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Transaction amount must be positive");
        }
        if (previousDebt == null || newDebt == null) {
            throw new BusinessException("Debts trace cannot be null");
        }
        if (registeredBy == null) throw new BusinessException("Registrar employee ID is required");

        CreditTransactionDomain txn = new CreditTransactionDomain();
        txn.creditAccountId = creditAccountId;
        txn.saleId = saleId;
        txn.type = type;
        txn.amount = amount;
        txn.previousDebt = previousDebt;
        txn.newDebt = newDebt;
        txn.notes = notes;
        txn.registeredBy = registeredBy;
        txn.createdAt = LocalDateTime.now();
        return txn;
    }

    /**
     * Reconstructs a credit transaction from persistence.
     */
    public static CreditTransactionDomain reconstruct(
            Long id, Long creditAccountId, Long saleId, String type, BigDecimal amount,
            BigDecimal previousDebt, BigDecimal newDebt, String notes, Long registeredBy, LocalDateTime createdAt) {
        CreditTransactionDomain txn = new CreditTransactionDomain();
        txn.id = id;
        txn.creditAccountId = creditAccountId;
        txn.saleId = saleId;
        txn.type = type;
        txn.amount = amount;
        txn.previousDebt = previousDebt;
        txn.newDebt = newDebt;
        txn.notes = notes;
        txn.registeredBy = registeredBy;
        txn.createdAt = createdAt;
        return txn;
    }

    // Getters

    public Long getId() { return id; }
    public Long getCreditAccountId() { return creditAccountId; }
    public Long getSaleId() { return saleId; }
    public String getType() { return type; }
    public BigDecimal getAmount() { return amount; }
    public BigDecimal getPreviousDebt() { return previousDebt; }
    public BigDecimal getNewDebt() { return newDebt; }
    public String getNotes() { return notes; }
    public Long getRegisteredBy() { return registeredBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
