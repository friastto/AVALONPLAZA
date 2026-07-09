package org.frias.avalon.domain.credit.application.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record UpdateCreditLimitRequest(
        @NotNull(message = "El ID de la cuenta es requerido")
        Long accountId,

        @NotNull(message = "El nuevo límite de crédito es requerido")
        @DecimalMin(value = "0.0", message = "El nuevo límite debe ser un monto positivo")
        BigDecimal newLimit
) {}
