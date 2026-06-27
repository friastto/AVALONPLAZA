package org.frias.avalon.domain.outlet.application.dto.response;

import java.util.List;

public record OutletDashboardResponse(
        KpiMetricsDto kpis,
        List<HourlySalesDto> hourlySales,
        List<StockAlertDto> alerts,
        List<CashRegisterMonitoringDto> activeRegisters
) {}
