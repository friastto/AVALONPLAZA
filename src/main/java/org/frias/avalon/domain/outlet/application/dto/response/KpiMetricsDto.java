package org.frias.avalon.domain.outlet.application.dto.response;

import java.math.BigDecimal;

public record KpiMetricsDto(
        BigDecimal totalCash,
        BigDecimal totalTransfer,
        int totalCustomers,
        BigDecimal averageTicket
) {}
