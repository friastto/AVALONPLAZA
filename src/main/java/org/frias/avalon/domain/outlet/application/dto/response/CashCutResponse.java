package org.frias.avalon.domain.outlet.application.dto.response;

import java.math.BigDecimal;

public record CashCutResponse(
        boolean success,
        String message,
        BigDecimal totalCutAmount,
        String timestamp
) {}
