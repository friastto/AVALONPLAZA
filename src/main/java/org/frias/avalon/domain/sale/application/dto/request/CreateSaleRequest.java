package org.frias.avalon.domain.sale.application.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

public record CreateSaleRequest(
        @NotNull(message = "El número de identificación del cliente es requerido")
        String clientNumberid,

        @NotNull(message = "El ID de la tienda (outlet) es requerido")
        Long outletId,

        @NotNull(message = "El ID del método de pago es requerido")
        Long paymentMethodId,

        BigDecimal amountReceived,

        @NotEmpty(message = "Debe registrar al menos un producto en la venta")
        List<SaleItemRequest> items
) {
}
