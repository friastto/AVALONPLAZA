package org.frias.avalon.domain.cashregister.infrastructure.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "cash_pickups")
public class CashPickupEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_id", nullable = false)
    private Long sessionId;

    @Column(name = "employee_id", nullable = false)
    private Long employeeId;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    private String reason;

    @Column(name = "pickup_time", nullable = false)
    private LocalDateTime pickupTime;

    public CashPickupEntity() {}

    public CashPickupEntity(Long id, Long sessionId, Long employeeId, BigDecimal amount, String reason, LocalDateTime pickupTime) {
        this.id = id;
        this.sessionId = sessionId;
        this.employeeId = employeeId;
        this.amount = amount;
        this.reason = reason;
        this.pickupTime = pickupTime;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private Long sessionId;
        private Long employeeId;
        private BigDecimal amount;
        private String reason;
        private LocalDateTime pickupTime;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder sessionId(Long sessionId) { this.sessionId = sessionId; return this; }
        public Builder employeeId(Long employeeId) { this.employeeId = employeeId; return this; }
        public Builder amount(BigDecimal amount) { this.amount = amount; return this; }
        public Builder reason(String reason) { this.reason = reason; return this; }
        public Builder pickupTime(LocalDateTime pickupTime) { this.pickupTime = pickupTime; return this; }

        public CashPickupEntity build() {
            return new CashPickupEntity(id, sessionId, employeeId, amount, reason, pickupTime);
        }
    }

    @PrePersist
    protected void onCreate() {
        if (this.pickupTime == null) {
            this.pickupTime = LocalDateTime.now();
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getSessionId() { return sessionId; }
    public void setSessionId(Long sessionId) { this.sessionId = sessionId; }
    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public LocalDateTime getPickupTime() { return pickupTime; }
    public void setPickupTime(LocalDateTime pickupTime) { this.pickupTime = pickupTime; }
}
