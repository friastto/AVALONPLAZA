package org.frias.avalon.domain.sale.application.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record CreateOrderRequest(
        Long paymentMethodId,
        String paymentMethodCode,

        @NotNull(message = "El ID de la tienda es requerido")
        Long outletId,

        @NotEmpty(message = "Debe registrar al menos un ítem en el pedido")
        List<OrderItemRequest> items
) {
    public CreateOrderRequest(Long paymentMethodId, Long outletId, List<OrderItemRequest> items) {
        this(paymentMethodId, null, outletId, items);
    }
}
