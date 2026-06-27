package org.frias.avalon.domain.outlet.application.usecase.update;

import org.frias.avalon.domain.outlet.application.dto.response.CashCutResponse;

/**
 * Puerto de Entrada del Caso de Uso para ejecutar el arqueo/corte de caja de una tienda.
 */
public interface ExecuteCashCutUseCase {
    
    /**
     * Realiza el cierre de turno y arqueo de caja de una tienda.
     *
     * @param outletId ID de la tienda.
     * @return El DTO con la confirmación de éxito y los montos arqueados.
     */
    CashCutResponse execute(Long outletId);
}
