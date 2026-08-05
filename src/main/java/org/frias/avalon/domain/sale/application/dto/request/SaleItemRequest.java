package org.frias.avalon.domain.sale.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record SaleItemRequest(
        @NotNull(message = "El ID del producto es requerido")
        Long productId,

        @NotBlank(message = "La cantidad del producto es requerida")
        String quantity,

        BigDecimal customLineTotal
) {
    public SaleItemRequest(Long productId, String quantity) {
        this(productId, quantity, null);
    }
}
