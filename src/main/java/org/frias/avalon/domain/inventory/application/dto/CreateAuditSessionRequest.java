package org.frias.avalon.domain.inventory.application.dto;

import jakarta.validation.constraints.NotNull;

public record CreateAuditSessionRequest(
        @NotNull(message = "El outletId es obligatorio")
        Long outletId,
        @NotNull(message = "El operatorId es obligatorio")
        Long operatorId,
        String operatorName,
        String notes
) {}
