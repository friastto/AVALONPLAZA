package org.frias.avalon.domain.product.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * DTO for creating a new product.
 * Stock quantity is received as a string to allow for flexible user input (e.g., "0.5" or "0,5").
 */
public record ProductNewDataRequest(
        @NotBlank(message = "Product barCode is required")

        String barCode,

        @NotBlank(message = "Product name is required")
    String name,


    String description,

    @NotBlank(message = "Stock quantity is required")
    String stockQuantity, // Se recibe como String para validación manual en el Use Case

    @NotNull(message = "Stock unit ID is required")
    Long stockUnitId,

    String imageUrl,

    @NotNull(message = "Price is required")
    BigDecimal price, // Se mantiene como BigDecimal para validación automática

    @NotNull(message = "Outlet ID is required")
    Long outletId
) {
}
