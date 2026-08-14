package org.frias.avalon.domain.cashregister.application.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class OpenCashSessionRequest {

    @NotNull(message = "El outletId es obligatorio")
    private Long outletId;

    @NotNull(message = "El employeeId es obligatorio")
    private Long employeeId;

    @NotNull(message = "La base inicial es obligatoria")
    @Min(value = 0, message = "La base inicial no puede ser negativa")
    private BigDecimal initialBase;
}
