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
 * Agregado raíz que representa un Pedido.
 */
public class OrderDomain {

    private final Long id;
    private final UUID orderCode;
    private final BigDecimal totalAmount;
    private final Long paymentMethodId;
    private Long statusId;
    private final Long outletId;
    private final LocalDateTime orderDate;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final List<OrderItemDomain> items;

    private OrderDomain(Long id, UUID orderCode, BigDecimal totalAmount, Long paymentMethodId, Long statusId, Long outletId,
                        LocalDateTime orderDate, LocalDateTime createdAt, LocalDateTime updatedAt, List<OrderItemDomain> items) {
        this.id = id;
        this.orderCode = orderCode;
        this.totalAmount = totalAmount;
        this.paymentMethodId = paymentMethodId;
        this.statusId = statusId;
        this.outletId = outletId;
        this.orderDate = orderDate;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.items = items != null ? new ArrayList<>(items) : new ArrayList<>();
    }

    /**
     * Crea un nuevo registro de Pedido.
     */
    public static OrderDomain create(
            Long paymentMethodId,
            Long statusId,
            Long outletId,
            List<OrderItemDomain> items
    ) {
        if (paymentMethodId == null || paymentMethodId <= 0) {
            throw new DomainValidationException("El método de pago es requerido");
        }
        if (statusId == null || statusId <= 0) {
            throw new DomainValidationException("El estado del pedido es requerido");
        }
        if (outletId == null || outletId <= 0) {
            throw new DomainValidationException("El outlet es requerido");
        }
        if (items == null || items.isEmpty()) {
            throw new DomainValidationException("Un pedido debe contener al menos un ítem");
        }

        BigDecimal total = BigDecimal.ZERO;
        for (OrderItemDomain item : items) {
            total = total.add(item.getSubtotal());
        }

        return new OrderDomain(
                null,
                UUID.randomUUID(),
                total,
                paymentMethodId,
                statusId,
                outletId,
                LocalDateTime.now(),
                LocalDateTime.now(),
                LocalDateTime.now(),
                items
        );
    }

    /**
     * Restaura un pedido desde la persistencia.
     */
    public static OrderDomain fromPersistence(
            Long id, UUID orderCode, BigDecimal totalAmount, Long paymentMethodId, Long statusId, Long outletId,
            LocalDateTime orderDate, LocalDateTime createdAt, LocalDateTime updatedAt, List<OrderItemDomain> items
    ) {
        return new OrderDomain(id, orderCode, totalAmount, paymentMethodId, statusId, outletId, orderDate, createdAt, updatedAt, items);
    }

    /**
     * Marca el pedido como facturado.
     */
    public void markAsInvoiced(Long invoicedStatusId) {
        if (invoicedStatusId == null || invoicedStatusId <= 0) {
            throw new DomainValidationException("El ID del estado de facturado es requerido");
        }
        this.statusId = invoicedStatusId;
    }

    public Long getId() {
        return id;
    }

    public UUID getOrderCode() {
        return orderCode;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public Long getPaymentMethodId() {
        return paymentMethodId;
    }

    public Long getStatusId() {
        return statusId;
    }

    public Long getOutletId() {
        return outletId;
    }

    public LocalDateTime getOrderDate() {
        return orderDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public List<OrderItemDomain> getItems() {
        return Collections.unmodifiableList(items);
    }
}
