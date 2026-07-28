package org.frias.avalon.domain.cashregister.domain;

import org.frias.avalon.core.exeptions.BusinessException;
import org.frias.avalon.core.exeptions.DomainValidationException;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Agregado raíz que representa una Sesión / Turno de Caja.
 */
public class CashSessionDomain {

    private final Long id;
    private final Long outletId;
    private final Long employeeId;
    private final LocalDateTime openedAt;
    private LocalDateTime closedAt;
    private final BigDecimal initialBase;
    private BigDecimal expectedCash;
    private BigDecimal actualCash;
    private BigDecimal difference;
    private String status; // "OPEN", "BLIND_COUNTED", "AUDITED", "CLOSED"
    private String notes;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private CashSessionDomain(
            Long id, Long outletId, Long employeeId, LocalDateTime openedAt, LocalDateTime closedAt,
            BigDecimal initialBase, BigDecimal expectedCash, BigDecimal actualCash, BigDecimal difference,
            String status, String notes, LocalDateTime createdAt, LocalDateTime updatedAt
    ) {
        this.id = id;
        this.outletId = outletId;
        this.employeeId = employeeId;
        this.openedAt = openedAt;
        this.closedAt = closedAt;
        this.initialBase = initialBase != null ? initialBase : BigDecimal.ZERO;
        this.expectedCash = expectedCash != null ? expectedCash : this.initialBase;
        this.actualCash = actualCash;
        this.difference = difference;
        this.status = status;
        this.notes = notes;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static CashSessionDomain open(Long outletId, Long employeeId, BigDecimal initialBase) {
        if (outletId == null || outletId <= 0) {
            throw new DomainValidationException("El id de la tienda (outletId) es requerido");
        }
        if (employeeId == null || employeeId <= 0) {
            throw new DomainValidationException("El id del empleado (employeeId) es requerido");
        }
        if (initialBase == null || initialBase.compareTo(BigDecimal.ZERO) < 0) {
            throw new DomainValidationException("La base inicial de caja no puede ser negativa");
        }

        LocalDateTime now = LocalDateTime.now();
        return new CashSessionDomain(
                null,
                outletId,
                employeeId,
                now,
                null,
                initialBase,
                initialBase, // expected initial cash is the base
                null,
                null,
                "OPEN",
                null,
                now,
                now
        );
    }

    public static CashSessionDomain fromPersistence(
            Long id, Long outletId, Long employeeId, LocalDateTime openedAt, LocalDateTime closedAt,
            BigDecimal initialBase, BigDecimal expectedCash, BigDecimal actualCash, BigDecimal difference,
            String status, String notes, LocalDateTime createdAt, LocalDateTime updatedAt
    ) {
        return new CashSessionDomain(
                id, outletId, employeeId, openedAt, closedAt, initialBase, expectedCash, actualCash,
                difference, status, notes, createdAt, updatedAt
        );
    }

    public void blindCount(BigDecimal actualCashContado, BigDecimal totalSalesCash, BigDecimal totalExpensesCash, BigDecimal totalPickups, String notes) {
        if (!"OPEN".equals(this.status)) {
            throw new BusinessException("La sesión de caja no se encuentra abierta");
        }
        if (actualCashContado == null || actualCashContado.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("El monto de efectivo contado no puede ser nulo o negativo");
        }

        BigDecimal salesCash = totalSalesCash != null ? totalSalesCash : BigDecimal.ZERO;
        BigDecimal expensesCash = totalExpensesCash != null ? totalExpensesCash : BigDecimal.ZERO;
        BigDecimal pickups = totalPickups != null ? totalPickups : BigDecimal.ZERO;

        // Efectivo Esperado = Base Inicial + Ventas en Efectivo - Egresos - Retiros
        this.expectedCash = this.initialBase.add(salesCash).subtract(expensesCash).subtract(pickups);
        this.actualCash = actualCashContado;
        // Diferencia = Efectivo Real Contado - Efectivo Esperado
        this.difference = this.actualCash.subtract(this.expectedCash);
        
        this.status = "BLIND_COUNTED";
        this.notes = notes;
        this.updatedAt = LocalDateTime.now();
    }

    public void audit() {
        if (!"BLIND_COUNTED".equals(this.status)) {
            throw new BusinessException("La sesión no está en estado de conteo ciego");
        }
        this.status = "AUDITED";
        this.updatedAt = LocalDateTime.now();
    }

    public void closeSession() {
        if (!"AUDITED".equals(this.status) && !"BLIND_COUNTED".equals(this.status)) {
            throw new BusinessException("La sesión no está lista para ser cerrada");
        }
        this.status = "CLOSED";
        this.closedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isThresholdExceeded(BigDecimal currentCash, BigDecimal cashThresholdAmount) {
        if (cashThresholdAmount == null || cashThresholdAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }
        if (currentCash == null) {
            return false;
        }
        return currentCash.compareTo(cashThresholdAmount) >= 0;
    }

    public Long getId() {
        return id;
    }

    public Long getOutletId() {
        return outletId;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public LocalDateTime getOpenedAt() {
        return openedAt;
    }

    public LocalDateTime getClosedAt() {
        return closedAt;
    }

    public BigDecimal getInitialBase() {
        return initialBase;
    }

    public BigDecimal getExpectedCash() {
        return expectedCash;
    }

    public BigDecimal getActualCash() {
        return actualCash;
    }

    public BigDecimal getDifference() {
        return difference;
    }

    public String getStatus() {
        return status;
    }

    public String getNotes() {
        return notes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
