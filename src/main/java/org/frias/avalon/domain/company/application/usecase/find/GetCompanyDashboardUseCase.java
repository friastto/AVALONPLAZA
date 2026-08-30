package org.frias.avalon.domain.company.application.usecase.find;

import org.frias.avalon.domain.company.application.dto.response.CompanyDashboardResponse;

/**
 * Input port for retrieving the Level 2 Company financial dashboard metrics.
 */
public interface GetCompanyDashboardUseCase {

    /**
     * Executes enterprise dashboard consolidation across all or specific company outlets.
     *
     * @param companyId  Unique identifier of the company.
     * @param period     Time filter ('HOY', 'MES', 'ANIO', 'HISTORICO').
     * @param outletId   Optional outlet ID for specific outlet drilldown.
     * @return Consolidated company dashboard response.
     */
    CompanyDashboardResponse execute(Long companyId, String period, Long outletId);
}
