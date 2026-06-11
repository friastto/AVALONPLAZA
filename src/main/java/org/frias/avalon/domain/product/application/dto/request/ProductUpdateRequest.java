package org.frias.avalon.domain.product.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

/**
 * DTO for updating an existing product.
 * All fields are required to ensure the client sends the full state of the product.
 * If partial updates were allowed, we would use Optional or different annotations.
 */
public record ProductUpdateRequest(
    @NotBlank(message = "Product name cannot be blank.")
    String name,

    String description,

    @NotBlank(message = "Stock quantity cannot be blank.")
    String stockQuantity,

    @NotNull(message = "Stock unit ID cannot be null.")
    Long stockUnitId,

    String imageUrl,

    @NotNull(message = "Price cannot be null.")
    @Positive(message = "Price must be positive.")
    BigDecimal price
) {}
