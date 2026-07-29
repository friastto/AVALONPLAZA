package org.frias.avalon.domain.cashregister.presentation.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CloseCashSessionRequest {

    @NotNull(message = "El monto de efectivo contado es obligatorio")
    @Min(value = 0, message = "El efectivo contado no puede ser negativo")
    private BigDecimal actualCash;

    private String notes;
}
