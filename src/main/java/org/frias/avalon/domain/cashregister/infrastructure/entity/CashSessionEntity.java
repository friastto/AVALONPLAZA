package org.frias.avalon.domain.cashregister.infrastructure.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "cash_sessions")
public class CashSessionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "outlet_id", nullable = false)
    private Long outletId;

    @Column(name = "employee_id", nullable = false)
    private Long employeeId;

    @Column(name = "opened_at", nullable = false)
    private LocalDateTime openedAt;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    @Column(name = "initial_base", nullable = false, precision = 12, scale = 2)
    private BigDecimal initialBase;

    @Column(name = "expected_cash", precision = 12, scale = 2)
    private BigDecimal expectedCash;

    @Column(name = "actual_cash", precision = 12, scale = 2)
    private BigDecimal actualCash;

    @Column(name = "difference", precision = 12, scale = 2)
    private BigDecimal difference;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public CashSessionEntity() {}

    public CashSessionEntity(Long id, Long outletId, Long employeeId, LocalDateTime openedAt, LocalDateTime closedAt, BigDecimal initialBase, BigDecimal expectedCash, BigDecimal actualCash, BigDecimal difference, String status, String notes, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.outletId = outletId;
        this.employeeId = employeeId;
        this.openedAt = openedAt;
        this.closedAt = closedAt;
        this.initialBase = initialBase;
        this.expectedCash = expectedCash;
        this.actualCash = actualCash;
        this.difference = difference;
        this.status = status;
        this.notes = notes;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private Long outletId;
        private Long employeeId;
        private LocalDateTime openedAt;
        private LocalDateTime closedAt;
        private BigDecimal initialBase;
        private BigDecimal expectedCash;
        private BigDecimal actualCash;
        private BigDecimal difference;
        private String status;
        private String notes;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder outletId(Long outletId) { this.outletId = outletId; return this; }
        public Builder employeeId(Long employeeId) { this.employeeId = employeeId; return this; }
        public Builder openedAt(LocalDateTime openedAt) { this.openedAt = openedAt; return this; }
        public Builder closedAt(LocalDateTime closedAt) { this.closedAt = closedAt; return this; }
        public Builder initialBase(BigDecimal initialBase) { this.initialBase = initialBase; return this; }
        public Builder expectedCash(BigDecimal expectedCash) { this.expectedCash = expectedCash; return this; }
        public Builder actualCash(BigDecimal actualCash) { this.actualCash = actualCash; return this; }
        public Builder difference(BigDecimal difference) { this.difference = difference; return this; }
        public Builder status(String status) { this.status = status; return this; }
        public Builder notes(String notes) { this.notes = notes; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public Builder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }

        public CashSessionEntity build() {
            return new CashSessionEntity(id, outletId, employeeId, openedAt, closedAt, initialBase, expectedCash, actualCash, difference, status, notes, createdAt, updatedAt);
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getOutletId() { return outletId; }
    public void setOutletId(Long outletId) { this.outletId = outletId; }
    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }
    public LocalDateTime getOpenedAt() { return openedAt; }
    public void setOpenedAt(LocalDateTime openedAt) { this.openedAt = openedAt; }
    public LocalDateTime getClosedAt() { return closedAt; }
    public void setClosedAt(LocalDateTime closedAt) { this.closedAt = closedAt; }
    public BigDecimal getInitialBase() { return initialBase; }
    public void setInitialBase(BigDecimal initialBase) { this.initialBase = initialBase; }
    public BigDecimal getExpectedCash() { return expectedCash; }
    public void setExpectedCash(BigDecimal expectedCash) { this.expectedCash = expectedCash; }
    public BigDecimal getActualCash() { return actualCash; }
    public void setActualCash(BigDecimal actualCash) { this.actualCash = actualCash; }
    public BigDecimal getDifference() { return difference; }
    public void setDifference(BigDecimal difference) { this.difference = difference; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
