package org.frias.avalon.domain.cashregister.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CashExpenseRequest {

    @NotNull(message = "El monto del egreso es obligatorio")
    @Positive(message = "El monto del egreso debe ser mayor a cero")
    private BigDecimal amount;

    @NotBlank(message = "El motivo del egreso es obligatorio")
    private String reason;

    @NotNull(message = "El id del usuario que registra es obligatorio")
    private Long registeredBy;
}
