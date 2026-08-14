package org.frias.avalon.domain.cashregister.infrastructure.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "cash_expenses")
public class CashExpenseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cash_session_id", nullable = false)
    private Long cashSessionId;

    @Column(name = "amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "reason", nullable = false, length = 255)
    private String reason;

    @Column(name = "registered_by", nullable = false)
    private Long registeredBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public CashExpenseEntity() {}

    public CashExpenseEntity(Long id, Long cashSessionId, BigDecimal amount, String reason, Long registeredBy, LocalDateTime createdAt) {
        this.id = id;
        this.cashSessionId = cashSessionId;
        this.amount = amount;
        this.reason = reason;
        this.registeredBy = registeredBy;
        this.createdAt = createdAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private Long cashSessionId;
        private BigDecimal amount;
        private String reason;
        private Long registeredBy;
        private LocalDateTime createdAt;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder cashSessionId(Long cashSessionId) { this.cashSessionId = cashSessionId; return this; }
        public Builder amount(BigDecimal amount) { this.amount = amount; return this; }
        public Builder reason(String reason) { this.reason = reason; return this; }
        public Builder registeredBy(Long registeredBy) { this.registeredBy = registeredBy; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public CashExpenseEntity build() {
            return new CashExpenseEntity(id, cashSessionId, amount, reason, registeredBy, createdAt);
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getCashSessionId() { return cashSessionId; }
    public void setCashSessionId(Long cashSessionId) { this.cashSessionId = cashSessionId; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public Long getRegisteredBy() { return registeredBy; }
    public void setRegisteredBy(Long registeredBy) { this.registeredBy = registeredBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
