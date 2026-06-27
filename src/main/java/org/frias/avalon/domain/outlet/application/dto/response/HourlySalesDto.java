package org.frias.avalon.domain.outlet.application.dto.response;

import java.math.BigDecimal;

public record HourlySalesDto(
        String hour,
        BigDecimal amount
) {}
