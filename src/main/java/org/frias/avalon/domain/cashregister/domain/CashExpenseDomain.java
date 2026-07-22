package org.frias.avalon.domain.cashregister.domain;

import org.frias.avalon.core.exeptions.DomainValidationException;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidad de dominio que representa un egreso o vale de caja menor.
 */
public class CashExpenseDomain {

    private final Long id;
    private final Long cashSessionId;
    private final BigDecimal amount;
    private final String reason;
    private final Long registeredBy;
    private final LocalDateTime createdAt;

    public CashExpenseDomain(Long id, Long cashSessionId, BigDecimal amount, String reason, Long registeredBy, LocalDateTime createdAt) {
        this.id = id;
        this.cashSessionId = cashSessionId;
        this.amount = amount;
        this.reason = reason;
        this.registeredBy = registeredBy;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
    }

    public static CashExpenseDomain create(Long cashSessionId, BigDecimal amount, String reason, Long registeredBy) {
        if (cashSessionId == null || cashSessionId <= 0) {
            throw new DomainValidationException("El id de la sesión de caja es requerido");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new DomainValidationException("El monto del egreso debe ser mayor a cero");
        }
        if (reason == null || reason.trim().isEmpty()) {
            throw new DomainValidationException("El motivo o descripción del egreso es requerido");
        }
        if (registeredBy == null || registeredBy <= 0) {
            throw new DomainValidationException("El id del usuario que registra el egreso es requerido");
        }

        return new CashExpenseDomain(null, cashSessionId, amount, reason.trim(), registeredBy, LocalDateTime.now());
    }

    public Long getId() {
        return id;
    }

    public Long getCashSessionId() {
        return cashSessionId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getReason() {
        return reason;
    }

    public Long getRegisteredBy() {
        return registeredBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
