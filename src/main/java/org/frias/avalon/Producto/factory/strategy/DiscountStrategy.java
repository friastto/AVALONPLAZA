package org.frias.avalon.Producto.factory.strategy;

import org.frias.avalon.maestra.entities.MasterData;

import java.math.BigDecimal;

public interface DiscountStrategy {

    BigDecimal applyDiscount(BigDecimal total);
}
