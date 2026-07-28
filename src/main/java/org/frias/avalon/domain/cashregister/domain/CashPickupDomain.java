package org.frias.avalon.domain.cashregister.domain;

import org.frias.avalon.core.exeptions.DomainValidationException;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CashPickupDomain {
    private final Long id;
    private final Long sessionId;
    private final Long employeeId;
    private final BigDecimal amount;
    private final String reason;
    private final LocalDateTime pickupTime;

    private CashPickupDomain(Long id, Long sessionId, Long employeeId, BigDecimal amount, String reason, LocalDateTime pickupTime) {
        this.id = id;
        this.sessionId = sessionId;
        this.employeeId = employeeId;
        this.amount = amount;
        this.reason = reason;
        this.pickupTime = pickupTime;
    }

    public static CashPickupDomain create(Long sessionId, Long employeeId, BigDecimal amount, String reason) {
        if (sessionId == null || sessionId <= 0) {
            throw new DomainValidationException("El id de la sesión es requerido");
        }
        if (employeeId == null || employeeId <= 0) {
            throw new DomainValidationException("El id del empleado/gerente es requerido");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new DomainValidationException("El monto del retiro debe ser mayor a cero");
        }

        return new CashPickupDomain(null, sessionId, employeeId, amount, reason, LocalDateTime.now());
    }

    public static CashPickupDomain fromPersistence(Long id, Long sessionId, Long employeeId, BigDecimal amount, String reason, LocalDateTime pickupTime) {
        return new CashPickupDomain(id, sessionId, employeeId, amount, reason, pickupTime);
    }

    public Long getId() { return id; }
    public Long getSessionId() { return sessionId; }
    public Long getEmployeeId() { return employeeId; }
    public BigDecimal getAmount() { return amount; }
    public String getReason() { return reason; }
    public LocalDateTime getPickupTime() { return pickupTime; }
}
