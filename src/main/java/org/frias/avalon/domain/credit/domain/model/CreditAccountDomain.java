package org.frias.avalon.domain.credit.domain.model;

import org.frias.avalon.core.exeptions.BusinessException;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Domain entity representing a client's credit account (Cartera de Fiado).
 * It manages credit limits and outstanding debt balances.
 */
public class CreditAccountDomain {

    private Long id;
    private Long clientId;
    private Long outletId;
    private BigDecimal creditLimit;
    private BigDecimal currentDebt;
    private Long statusId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private CreditAccountDomain() {}

    /**
     * Creates a new Credit Account with a default limit.
     *
     * @param clientId The ID of the client (Person).
     * @param outletId The ID of the outlet.
     * @param creditLimit The credit limit.
     * @param statusId The status (e.g., Active).
     * @return A new instance of CreditAccountDomain.
     */
    public static CreditAccountDomain create(Long clientId, Long outletId, BigDecimal creditLimit, Long statusId) {
        if (clientId == null) throw new BusinessException("Client ID is required to create a credit account");
        if (outletId == null) throw new BusinessException("Outlet ID is required to create a credit account");
        if (creditLimit == null || creditLimit.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("Credit limit must be a positive amount");
        }
        if (statusId == null) throw new BusinessException("Status ID is required to create a credit account");

        CreditAccountDomain account = new CreditAccountDomain();
        account.clientId = clientId;
        account.outletId = outletId;
        account.creditLimit = creditLimit;
        account.currentDebt = BigDecimal.ZERO;
        account.statusId = statusId;
        account.createdAt = LocalDateTime.now();
        account.updatedAt = LocalDateTime.now();
        return account;
    }

    /**
     * Reconstructs a Credit Account from persistence state.
     */
    public static CreditAccountDomain reconstruct(
            Long id, Long clientId, Long outletId, BigDecimal creditLimit,
            BigDecimal currentDebt, Long statusId, LocalDateTime createdAt, LocalDateTime updatedAt) {
        CreditAccountDomain account = new CreditAccountDomain();
        account.id = id;
        account.clientId = clientId;
        account.outletId = outletId;
        account.creditLimit = creditLimit;
        account.currentDebt = currentDebt;
        account.statusId = statusId;
        account.createdAt = createdAt;
        account.updatedAt = updatedAt;
        return account;
    }

    /**
     * Charges a new debt amount to the account, checking limits.
     *
     * @param amount The purchase amount to charge.
     * @throws BusinessException If limit is exceeded.
     */
    public void charge(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Charge amount must be positive");
        }
        BigDecimal projectedDebt = this.currentDebt.add(amount);
        if (projectedDebt.compareTo(this.creditLimit) > 0) {
            throw new BusinessException("Credit limit exceeded. Current debt: $" + this.currentDebt 
                + ", Purchase: $" + amount + ", Limit: $" + this.creditLimit);
        }
        this.currentDebt = projectedDebt;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Pays/reduces outstanding debt.
     *
     * @param amount The payment amount.
     */
    public void pay(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Payment amount must be positive");
        }
        if (amount.compareTo(this.currentDebt) > 0) {
            throw new BusinessException("Payment amount ($" + amount + ") cannot exceed current debt ($" + this.currentDebt + ")");
        }
        this.currentDebt = this.currentDebt.subtract(amount);
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Updates the credit limit.
     *
     * @param newLimit The new credit limit.
     */
    public void updateLimit(BigDecimal newLimit) {
        if (newLimit == null || newLimit.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("New credit limit must be positive");
        }
        this.creditLimit = newLimit;
        this.updatedAt = LocalDateTime.now();
    }

    // Getters

    public Long getId() { return id; }
    public Long getClientId() { return clientId; }
    public Long getOutletId() { return outletId; }
    public BigDecimal getCreditLimit() { return creditLimit; }
    public BigDecimal getCurrentDebt() { return currentDebt; }
    public Long getStatusId() { return statusId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
