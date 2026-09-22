package org.frias.avalon.domain.inventory.application.dto;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record AuditItemCountRequest(
        @NotNull(message = "El auditSessionId es obligatorio")
        Long auditSessionId,
        @NotNull(message = "El productOutletId es obligatorio")
        Long productOutletId,
        @NotNull(message = "La cantidad fisica es obligatoria")
        BigDecimal physicalStock,
        String discrepancyReason,
        @NotNull(message = "El operatorId es obligatorio")
        Long operatorId,
        String operatorName
) {}
