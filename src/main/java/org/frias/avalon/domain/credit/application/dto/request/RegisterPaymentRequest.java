package org.frias.avalon.domain.credit.application.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record RegisterPaymentRequest(
        @NotBlank(message = "El número de identificación del cliente es requerido")
        String clientNumberid,

        @NotNull(message = "El ID de la tienda es requerido")
        Long outletId,

        @NotNull(message = "El monto del abono es requerido")
        @DecimalMin(value = "0.01", message = "El abono debe ser mayor a cero")
        BigDecimal amount,

        String notes
) {}
