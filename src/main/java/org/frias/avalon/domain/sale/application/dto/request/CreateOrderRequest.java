package org.frias.avalon.domain.sale.application.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record CreateOrderRequest(
        @NotNull(message = "El ID del método de pago es requerido")
        Long paymentMethodId,

        @NotNull(message = "El ID de la tienda es requerido")
        Long outletId,

        @NotEmpty(message = "Debe registrar al menos un ítem en el pedido")
        List<OrderItemRequest> items
) {
}
