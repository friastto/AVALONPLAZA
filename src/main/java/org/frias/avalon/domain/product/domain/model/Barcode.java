package org.frias.avalon.domain.product.domain.model;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class Barcode {
    private Long id;
    private Long productId;
    private String code;

    public Barcode(Long productId, String code) {
        this.productId = productId;
        this.code = code;
    }

    public static Barcode create(Long productId, String code) {
        if (productId == null) {
            throw new IllegalArgumentException("El ID del producto no puede ser nulo");
        }
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("El código de barras no puede ser nulo o vacío");
        }
        return new Barcode(productId, code);
    }
}
