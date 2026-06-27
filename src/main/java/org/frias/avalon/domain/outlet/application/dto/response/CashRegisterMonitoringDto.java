package org.frias.avalon.domain.outlet.application.dto.response;

import java.math.BigDecimal;

public record CashRegisterMonitoringDto(
        String employeeName,
        String role,
        boolean isOpen,
        BigDecimal currentCash
) {}
