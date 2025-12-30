package org.frias.avalon.promociones.factory.oters.interfaz;

import org.frias.avalon.promociones.dtos.DiscountTempResult;
import org.frias.avalon.promociones.entities.Promotion;

import java.math.BigDecimal;

public interface PromotionStrategy {
    DiscountTempResult applyDiscount(BigDecimal basePrice, Promotion promo);
    String getPromotionType(); // Para identificarla (ej: "PERCENTAGE", "FIXED")
}