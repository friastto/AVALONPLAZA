package org.frias.avalon.domain.outlet.application.usecase.find;

import org.frias.avalon.domain.outlet.application.dto.response.OutletDashboardResponse;

/**
 * Puerto de Entrada del Caso de Uso para obtener las métricas y alertas del Dashboard de un Outlet.
 */
public interface GetOutletDashboardUseCase {
    
    /**
     * Obtiene el consolidado del dashboard de una tienda aplicando un filtro temporal y consultando la base de datos real.
     *
     * @param outletId ID de la tienda.
     * @param filter Filtro temporal ("HOY", "AYER", "SEMANA").
     * @return El DTO con los KPIs, ventas por hora, alertas e información de cajas.
     */
    OutletDashboardResponse execute(Long outletId, String filter);
}
