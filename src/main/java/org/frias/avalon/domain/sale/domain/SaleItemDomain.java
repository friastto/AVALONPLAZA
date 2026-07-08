package org.frias.avalon.domain.sale.domain;

import org.frias.avalon.core.exeptions.DomainValidationException;
import java.math.BigDecimal;

/**
 * Entidad de dominio que representa un ítem de venta.
 * Inmutable.
 */
public class SaleItemDomain {

    private final Long id;
    private final Long productId;
    private final Integer quantityInBaseUnits;
    private final String displayQuantity;
    private final BigDecimal unitPrice;
    private final BigDecimal subtotal;
    private final Long unitMeasureId;

    public SaleItemDomain(Long id, Long productId, Integer quantityInBaseUnits, String displayQuantity, BigDecimal unitPrice, BigDecimal subtotal, Long unitMeasureId) {
        if (productId == null || productId <= 0) {
            throw new DomainValidationException("El ID de producto es requerido");
        }
        if (quantityInBaseUnits == null || quantityInBaseUnits <= 0) {
            throw new DomainValidationException("La cantidad debe ser mayor a cero");
        }
        if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new DomainValidationException("El precio unitario no puede ser negativo");
        }
        if (subtotal == null || subtotal.compareTo(BigDecimal.ZERO) < 0) {
            throw new DomainValidationException("El subtotal no puede ser negativo");
        }
        if (unitMeasureId == null || unitMeasureId <= 0) {
            throw new DomainValidationException("La unidad de medida es requerida");
        }

        this.id = id;
        this.productId = productId;
        this.quantityInBaseUnits = quantityInBaseUnits;
        this.displayQuantity = displayQuantity;
        this.unitPrice = unitPrice;
        this.subtotal = subtotal;
        this.unitMeasureId = unitMeasureId;
    }

    public Long getId() {
        return id;
    }

    public Long getProductId() {
        return productId;
    }

    public Integer getQuantityInBaseUnits() {
        return quantityInBaseUnits;
    }

    public String getDisplayQuantity() {
        return displayQuantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public Long getUnitMeasureId() {
        return unitMeasureId;
    }
}
