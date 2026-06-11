package org.frias.avalon.domain.product.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record LinkBarcodeRequest(
        @NotNull(message = "Product ID cannot be null")
        Long productId,

        @NotBlank(message = "Barcode cannot be blank")
        String barcode,

        String description
) {
}
