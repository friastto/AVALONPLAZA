package org.frias.avalon.domain.product.domain;

import org.frias.avalon.core.exeptions.DomainValidationException;

import java.time.LocalDateTime;

/**
 * Pure Java domain entity representing a barcode associated with a product.
 * Part of Product Aggregate.
 */
public class BarcodeDomain {

    private final Long id;
    private final String barcode;
    private final Long productOutletId;
    private final String description;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private BarcodeDomain(Long id, String barcode, Long productOutletId, String description, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.barcode = barcode;
        this.productOutletId = productOutletId;
        this.description = description;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static BarcodeDomain create(String barcode, Long productOutletId, String description) {
        if (barcode == null || barcode.isBlank()) {
            throw new DomainValidationException("Barcode cannot be blank");
        }
        if (productOutletId == null || productOutletId <= 0) {
            throw new DomainValidationException("Barcode must be assigned to a valid product");
        }

        return new BarcodeDomain(
                null,
                barcode.trim(),
                productOutletId,
                description,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    public static BarcodeDomain fromPersistence(Long id, String barcode, Long productOutletId, String description, LocalDateTime createdAt, LocalDateTime updatedAt) {
        return new BarcodeDomain(id, barcode, productOutletId, description, createdAt, updatedAt);
    }

    public Long getId() { return id; }
    public String getBarcode() { return barcode; }
    public Long getProductOutletId() { return productOutletId; }
    public String getDescription() { return description; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
