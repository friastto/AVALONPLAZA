package org.frias.avalon.domain.sale.domain;

import org.frias.avalon.core.exeptions.DomainValidationException;
import org.frias.avalon.core.exeptions.BusinessException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Agregado raíz que representa una Venta.
 */
public class SaleDomain {

    private final Long id;
    private final UUID saleCode;
    private final BigDecimal totalAmount;
    private BigDecimal amountReceived;
    private BigDecimal changeGiven;
    private final Long paymentMethodId;
    private final Long statusId;
    private final Long clientId;
    private final Long outletId;
    private final Long employeeId;
    private final LocalDateTime saleDate;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final List<SaleItemDomain> items;

    private SaleDomain(Long id, UUID saleCode, BigDecimal totalAmount, BigDecimal amountReceived, BigDecimal changeGiven,
                       Long paymentMethodId, Long statusId, Long clientId, Long outletId, Long employeeId,
                       LocalDateTime saleDate, LocalDateTime createdAt, LocalDateTime updatedAt, List<SaleItemDomain> items) {
        this.id = id;
        this.saleCode = saleCode;
        this.totalAmount = totalAmount;
        this.amountReceived = amountReceived;
        this.changeGiven = changeGiven;
        this.paymentMethodId = paymentMethodId;
        this.statusId = statusId;
        this.clientId = clientId;
        this.outletId = outletId;
        this.employeeId = employeeId;
        this.saleDate = saleDate;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.items = items != null ? new ArrayList<>(items) : new ArrayList<>();
    }

    /**
     * Crea un nuevo registro de Venta aplicando invariantes.
     */
    public static SaleDomain create(
            Long paymentMethodId,
            Long statusId,
            Long clientId,
            Long outletId,
            Long employeeId,
            List<SaleItemDomain> items
    ) {
        if (paymentMethodId == null || paymentMethodId <= 0) {
            throw new DomainValidationException("El método de pago es requerido");
        }
        if (statusId == null || statusId <= 0) {
            throw new DomainValidationException("El estado de la venta es requerido");
        }
        if (clientId == null || clientId <= 0) {
            throw new DomainValidationException("El cliente es requerido");
        }
        if (outletId == null || outletId <= 0) {
            throw new DomainValidationException("El outlet es requerido");
        }
        if (employeeId == null || employeeId <= 0) {
            throw new DomainValidationException("El empleado es requerido");
        }
        if (items == null || items.isEmpty()) {
            throw new DomainValidationException("Una venta debe contener al menos un ítem");
        }

        BigDecimal total = BigDecimal.ZERO;
        for (SaleItemDomain item : items) {
            total = total.add(item.getSubtotal());
        }

        return new SaleDomain(
                null,
                UUID.randomUUID(),
                total,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                paymentMethodId,
                statusId,
                clientId,
                outletId,
                employeeId,
                LocalDateTime.now(),
                LocalDateTime.now(),
                LocalDateTime.now(),
                items
        );
    }

    /**
     * Restaura una venta desde persistencia.
     */
    public static SaleDomain fromPersistence(
            Long id, UUID saleCode, BigDecimal totalAmount, BigDecimal amountReceived, BigDecimal changeGiven,
            Long paymentMethodId, Long statusId, Long clientId, Long outletId, Long employeeId,
            LocalDateTime saleDate, LocalDateTime createdAt, LocalDateTime updatedAt, List<SaleItemDomain> items
    ) {
        return new SaleDomain(id, saleCode, totalAmount, amountReceived, changeGiven, paymentMethodId, statusId, clientId, outletId, employeeId, saleDate, createdAt, updatedAt, items);
    }

    /**
     * Aplica el pago de la venta y calcula el cambio/devuelto.
     * Corrige el bug de devuelto invertido: changeGiven = amountReceived - totalAmount.
     */
    public void applyPayment(BigDecimal amountReceived) {
        applyPayment(amountReceived, false);
    }

    public void applyPayment(BigDecimal amountReceived, boolean isFiado) {
        if (amountReceived == null || amountReceived.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("El monto recibido no puede ser negativo o nulo");
        }
        if (!isFiado && amountReceived.compareTo(this.totalAmount) < 0) {
            throw new BusinessException("El monto recibido (" + amountReceived + ") es menor que el valor total a pagar (" + this.totalAmount + ")");
        }
        this.amountReceived = amountReceived;
        if (isFiado) {
            this.changeGiven = BigDecimal.ZERO;
        } else {
            this.changeGiven = amountReceived.subtract(this.totalAmount);
        }
    }

    public Long getId() {
        return id;
    }

    public UUID getSaleCode() {
        return saleCode;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public BigDecimal getAmountReceived() {
        return amountReceived;
    }

    public BigDecimal getChangeGiven() {
        return changeGiven;
    }

    public Long getPaymentMethodId() {
        return paymentMethodId;
    }

    public Long getStatusId() {
        return statusId;
    }

    public Long getClientId() {
        return clientId;
    }

    public Long getOutletId() {
        return outletId;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public LocalDateTime getSaleDate() {
        return saleDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public List<SaleItemDomain> getItems() {
        return Collections.unmodifiableList(items);
    }
}
