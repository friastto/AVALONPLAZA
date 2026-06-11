package org.frias.avalon.domain.product.domain;

import org.frias.avalon.core.exeptions.DomainValidationException;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * Entidad que representa un código de barras asociado a un producto.
 * Es parte del agregado Product.
 */
@Getter
public class BarcodeDomain {

    private final Long id;
    private final String barcode;
    private final Long productOutletId; // A qué producto pertenece
    private final String description;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructor privado para forzar el uso de Factory Methods
    private BarcodeDomain(Long id, String barcode, Long productOutletId, String description, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.barcode = barcode;
        this.productOutletId = productOutletId;
        this.description = description;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * Factory Method para crear un nuevo código de barras.
     */
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

    /**
     * Restaura la entidad desde la base de datos.
     */
    public static BarcodeDomain fromPersistence(Long id, String barcode, Long productOutletId, String description, LocalDateTime createdAt, LocalDateTime updatedAt) {
        return new BarcodeDomain(id, barcode, productOutletId, description, createdAt, updatedAt);
    }

    // No hay métodos de comportamiento de negocio para Barcode por ahora,
    // ya que se considera mayormente inmutable una vez creado.
    // Si se necesitara cambiar la descripción, se añadiría un método aquí.
    // public void updateDescription(String newDescription) { ... }
}
