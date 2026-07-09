package org.frias.avalon.domain.credit.application.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record CreateCreditAccountRequest(
        @NotBlank(message = "El número de identificación del cliente es requerido")
        String clientNumberid,

        @NotNull(message = "El ID de la tienda es requerido")
        Long outletId,

        @NotNull(message = "El límite de crédito es requerido")
        @DecimalMin(value = "0.0", message = "El límite de crédito debe ser un monto positivo")
        BigDecimal creditLimit
) {}
