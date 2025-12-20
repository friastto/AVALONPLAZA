package org.frias.avalon.ventas.dtos;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

public record SaleRequest (

        BigDecimal amountReceived,

        Long metodoPagoId,

        @NotNull(message = "El empleado debe asignarse a la venta")
        Long enployeeId,

        String customerId,

        Long estadoId,

        @NotNull(message = "la venta debe tener productos en la cuenta")
        List<SaleDetailRequest> saleDetails




){
}
