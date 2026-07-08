package org.frias.avalon.domain.sale.domain.service;

import java.math.BigDecimal;

/**
 * Servicio de dominio responsable de convertir cantidades entre diferentes unidades de medida.
 * Soporta unidades pesables como KG, GR, LB, LT y unidades enteras como UND.
 */
public interface SaleWeightConversionService {

    /**
     * Convierte una cantidad dada con su unidad a la unidad base más pequeña como entero.
     * Por ejemplo, KG -> Gramos, LT -> Mililitros.
     *
     * @param quantity La cantidad numérica.
     * @param unitShortName El nombre corto de la unidad (ej. "KG", "LB", "LT").
     * @return La cantidad convertida en la unidad base.
     */
    Integer convertToBaseUnit(BigDecimal quantity, String unitShortName);

    /**
     * Convierte una cantidad en su unidad base a una cadena legible para humanos en la unidad indicada.
     *
     * @param baseQuantity La cantidad en su unidad base.
     * @param unitShortName El nombre corto de la unidad de destino.
     * @return Una representación formateada (ej. "1.5 KG").
     */
    String formatFromBaseUnit(Integer baseQuantity, String unitShortName);

    /**
     * Determina si una unidad de medida es pesable o de volumen.
     *
     * @param unitShortName El nombre corto de la unidad.
     * @return true si es pesable/volumen, false en caso contrario.
     */
    boolean isWeighable(String unitShortName);
}
