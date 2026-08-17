package org.frias.avalon.domain.order.application.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderRequest {
    private Long customerId;

    @NotNull(message = "El outletId es obligatorio")
    private Long outletId;

    private Long paymentMethodId;

    @NotEmpty(message = "El pedido debe contener al menos un item")
    private List<OrderItemRequest> items;
}
