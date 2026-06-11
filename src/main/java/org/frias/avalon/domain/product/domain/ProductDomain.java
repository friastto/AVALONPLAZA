package org.frias.avalon.domain.product.domain;

import org.frias.avalon.core.exeptions.DomainValidationException;
import lombok.Getter;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;

/**
 * Entidad core del agregado Product.
 * Diseño Rico.
 */
@Getter
public class ProductDomain {

    private final Long id;
    private String name;
    private String description;
    private Integer stock;
    private Long unitMeasureId; // Permitimos actualización de la unidad de medida
    private String imageUrl;
    private BigDecimal price;
    private final Long outletId; // El outletId generalmente no cambia una vez creado
    private Long statusId;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructor privado para forzar el uso de Factory Methods
    private ProductDomain(Long id, String name, String description, Integer stock, Long unitMeasureId, String imageUrl, BigDecimal price, Long outletId, Long statusId, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.stock = stock;
        this.unitMeasureId = unitMeasureId;
        this.imageUrl = imageUrl;
        this.price = price;
        this.outletId = outletId;
        this.statusId = statusId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * Factory Method para la creación de un nuevo producto.
     * Valida las invariantes críticas de negocio antes de instanciar.
     */
    public static ProductDomain create(
            String name,
            String description,
            Integer initialStock,
            Long unitMeasureId,
            String imageUrl,
            BigDecimal initialPrice,
            Long outletId,
            Long activeStatusId
    ) {
        if (name == null || name.isBlank()) {
            throw new DomainValidationException("Product name is required");
        }
        if (outletId == null || outletId <= 0) {
            throw new DomainValidationException("Product must be assigned to a valid outlet");
        }
        if (unitMeasureId == null || unitMeasureId <= 0) {
            throw new DomainValidationException("Unit of measurement is required");
        }
        if (activeStatusId == null) {
            throw new DomainValidationException("Status ID is required for creation");
        }

        Integer validStock = (initialStock == null || initialStock < 0) ? 0 : initialStock;
        BigDecimal validPrice = (initialPrice == null) ? BigDecimal.ZERO : initialPrice;

        return new ProductDomain(
                null, 
                name.trim(), 
                description, 
                validStock,
                unitMeasureId,
                imageUrl, 
                validPrice, 
                outletId, 
                activeStatusId,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    /**
     * Restaura la entidad desde la base de datos sin disparar validaciones de creación.
     */
    public static ProductDomain fromPersistence(
            Long id, String name, String description, Integer stock, Long unitMeasureId, String imageUrl, BigDecimal price, Long outletId, Long statusId, LocalDateTime createdAt, LocalDateTime updatedAt
    ) {
        return new ProductDomain(id, name, description, stock, unitMeasureId, imageUrl, price, outletId, statusId, createdAt, updatedAt);
    }

    // =========================================================================
    // Métodos de Comportamiento de Negocio (Rich Domain Model)
    // =========================================================================

    public void updateDetails(String name, String description, Integer stockInBaseUnits, Long unitMeasureId, String imageUrl, BigDecimal price) {
        if (name == null || name.isBlank()) {
            throw new DomainValidationException("Product name cannot be blank");
        }
        if (unitMeasureId == null || unitMeasureId <= 0) {
            throw new DomainValidationException("Unit of measurement is required");
        }
        if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
            throw new DomainValidationException("Price cannot be null or negative");
        }
        if (stockInBaseUnits == null || stockInBaseUnits < 0) {
            throw new DomainValidationException("Stock cannot be negative");
        }

        this.name = name.trim();
        this.description = description;
        this.stock = stockInBaseUnits;
        this.unitMeasureId = unitMeasureId;
        this.imageUrl = imageUrl;
        this.price = price;
        this.updatedAt = LocalDateTime.now();
    }

    public void updatePrice(BigDecimal newPrice) {
        if (newPrice == null || newPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new DomainValidationException("Price cannot be null or negative");
        }
        this.price = newPrice;
        this.updatedAt = LocalDateTime.now();
    }

    public void addStock(int quantity) {
        if (quantity <= 0) {
            throw new DomainValidationException("Quantity to add must be strictly positive");
        }
        this.stock += quantity;
        this.updatedAt = LocalDateTime.now();
    }

    public void removeStock(int quantity) {
        if (quantity <= 0) {
            throw new DomainValidationException("Quantity to remove must be strictly positive");
        }
        if (this.stock - quantity < 0) {
            throw new DomainValidationException("Insufficient stock for product: " + this.name);
        }
        this.stock -= quantity;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateImage(String newImageUrl) {
        if (newImageUrl == null || newImageUrl.isBlank()) {
            throw new DomainValidationException("Image URL cannot be blank");
        }
        this.imageUrl = newImageUrl;
        this.updatedAt = LocalDateTime.now();
    }

    public void changeStatus(Long newStatusId) {
        if (newStatusId == null) {
            throw new DomainValidationException("New status ID cannot be null");
        }
        this.statusId = newStatusId;
        this.updatedAt = LocalDateTime.now();
    }
}
