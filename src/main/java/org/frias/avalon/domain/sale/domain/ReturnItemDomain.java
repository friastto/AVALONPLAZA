package org.frias.avalon.domain.sale.domain;

import java.math.BigDecimal;

public class ReturnItemDomain {

    private final Long id;
    private final Long productId;
    private final Integer quantityInBaseUnits;
    private final String displayQuantity;
    private final BigDecimal unitPrice;
    private final BigDecimal subtotal;
    private final Long unitMeasureId;

    public ReturnItemDomain(Long id, Long productId, Integer quantityInBaseUnits,
                            String displayQuantity, BigDecimal unitPrice,
                            BigDecimal subtotal, Long unitMeasureId) {
        this.id = id;
        this.productId = productId;
        this.quantityInBaseUnits = quantityInBaseUnits;
        this.displayQuantity = displayQuantity;
        this.unitPrice = unitPrice;
        this.subtotal = subtotal;
        this.unitMeasureId = unitMeasureId;
    }

    public Long getId() { return id; }
    public Long getProductId() { return productId; }
    public Integer getQuantityInBaseUnits() { return quantityInBaseUnits; }
    public String getDisplayQuantity() { return displayQuantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public BigDecimal getSubtotal() { return subtotal; }
    public Long getUnitMeasureId() { return unitMeasureId; }
}
