package org.frias.avalon.promociones.factory.interfaz;

import org.frias.avalon.promociones.entities.Promotion;

import java.math.BigDecimal;

public interface PromotionStrategy {
    BigDecimal apply(BigDecimal basePrice, Promotion promo);
    String getPromotionType(); // Para identificarla (ej: "PERCENTAGE", "FIXED")
}