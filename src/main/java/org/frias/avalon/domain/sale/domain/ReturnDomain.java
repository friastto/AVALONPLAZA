package org.frias.avalon.domain.sale.domain;

import org.frias.avalon.core.exeptions.DomainValidationException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Agregado raíz que representa una Devolución / Cambio de productos en POS.
 *
 * resolutionType válidos: REEMBOLSO | NOTA_CREDITO | CAMBIO
 * reason válidos: DEFECTO | INCORRECTO | OTRO
 */
public class ReturnDomain {

    private final Long id;
    private final UUID returnCode;
    private final Long originalSaleId;
    private final BigDecimal totalRefundAmount;
    private final String reason;
    private final String notes;
    private final String resolutionType;
    private final Long statusId;
    private final Long employeeId;
    private final Long outletId;
    private final Long clientId;
    private final LocalDateTime returnDate;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final List<ReturnItemDomain> items;

    private ReturnDomain(Long id, UUID returnCode, Long originalSaleId,
                         BigDecimal totalRefundAmount, String reason, String notes, String resolutionType,
                         Long statusId, Long employeeId, Long outletId, Long clientId,
                         LocalDateTime returnDate, LocalDateTime createdAt,
                         LocalDateTime updatedAt, List<ReturnItemDomain> items) {
        this.id = id;
        this.returnCode = returnCode;
        this.originalSaleId = originalSaleId;
        this.totalRefundAmount = totalRefundAmount;
        this.reason = reason;
        this.notes = notes;
        this.resolutionType = resolutionType;
        this.statusId = statusId;
        this.employeeId = employeeId;
        this.outletId = outletId;
        this.clientId = clientId;
        this.returnDate = returnDate;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.items = items != null ? new ArrayList<>(items) : new ArrayList<>();
    }

    public static ReturnDomain create(Long originalSaleId, String reason, String notes, String resolutionType,
                                      Long statusId, Long employeeId, Long outletId, Long clientId,
                                      List<ReturnItemDomain> items) {
        if (originalSaleId == null)
            throw new DomainValidationException("La venta original es requerida");
        if (reason == null || reason.isBlank())
            throw new DomainValidationException("El motivo de devolución es requerido");
        if (resolutionType == null || resolutionType.isBlank())
            throw new DomainValidationException("El tipo de resolución es requerido");
        if (items == null || items.isEmpty())
            throw new DomainValidationException("Una devolución debe tener al menos un ítem");

        List<String> validReasons = List.of("DEFECTO", "INCORRECTO", "OTRO");
        if (!validReasons.contains(reason.toUpperCase()))
            throw new DomainValidationException("Motivo inválido. Use: DEFECTO, INCORRECTO o OTRO");

        List<String> validResolutions = List.of("REEMBOLSO", "NOTA_CREDITO", "CAMBIO");
        if (!validResolutions.contains(resolutionType.toUpperCase()))
            throw new DomainValidationException("Resolución inválida. Use: REEMBOLSO, NOTA_CREDITO o CAMBIO");

        BigDecimal total = items.stream()
                .map(ReturnItemDomain::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        LocalDateTime now = LocalDateTime.now();
        return new ReturnDomain(null, UUID.randomUUID(), originalSaleId, total,
                reason.toUpperCase(), notes, resolutionType.toUpperCase(),
                statusId, employeeId, outletId, clientId,
                now, now, now, items);
    }

    public static ReturnDomain fromPersistence(Long id, UUID returnCode, Long originalSaleId,
                                               BigDecimal totalRefundAmount, String reason, String notes,
                                               String resolutionType, Long statusId, Long employeeId,
                                               Long outletId, Long clientId, LocalDateTime returnDate,
                                               LocalDateTime createdAt, LocalDateTime updatedAt,
                                               List<ReturnItemDomain> items) {
        return new ReturnDomain(id, returnCode, originalSaleId, totalRefundAmount, reason, notes,
                resolutionType, statusId, employeeId, outletId, clientId,
                returnDate, createdAt, updatedAt, items);
    }

    public Long getId() { return id; }
    public UUID getReturnCode() { return returnCode; }
    public Long getOriginalSaleId() { return originalSaleId; }
    public BigDecimal getTotalRefundAmount() { return totalRefundAmount; }
    public String getReason() { return reason; }
    public String getNotes() { return notes; }
    public String getResolutionType() { return resolutionType; }
    public Long getStatusId() { return statusId; }
    public Long getEmployeeId() { return employeeId; }
    public Long getOutletId() { return outletId; }
    public Long getClientId() { return clientId; }
    public LocalDateTime getReturnDate() { return returnDate; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public List<ReturnItemDomain> getItems() { return Collections.unmodifiableList(items); }
}
