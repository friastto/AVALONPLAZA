package org.frias.avalon.domain.product.domain.model;

/**
 * Pure Java Domain model representing a Barcode in ApiAvalon.
 * Free of Lombok annotations.
 */
public class Barcode {
    private Long id;
    private Long productId;
    private String code;

    public Barcode() {
    }

    public Barcode(Long productId, String code) {
        this.productId = productId;
        this.code = code;
    }

    public static Barcode create(Long productId, String code) {
        if (productId == null) {
            throw new IllegalArgumentException("El ID del producto no puede ser nulo");
        }
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("El codigo de barras no puede ser nulo o vacio");
        }
        return new Barcode(productId, code);
    }

    public Long getId() { return id; }
    public Long getProductId() { return productId; }
    public String getCode() { return code; }
}
