package org.frias.avalon.temp.inventory.promo.factory.oters.interfaz;

import org.frias.avalon.temp.inventory.promo.dtos.DiscountTempResult;
import org.frias.avalon.temp.inventory.promo.entities.Promotion;

import java.math.BigDecimal;

public interface PromotionStrategy {
    DiscountTempResult applyDiscount(BigDecimal basePrice, Promotion promo);
    String getPromotionType(); // Para identificarla (ej: "PERCENTAGE", "FIXED")
}