package org.frias.avalon.domain.product.infraestructure.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Entidad JPA para el Catalogo Global Maestro de Avalon (Nivel 1).
 * Almacenada en el esquema 'public.products'.
 * Sigue la regla arquitectonica de claves foraneas planas sin JOINs pesados a master_data.
 */
@Entity
@Table(name = "products", schema = "public")
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(name = "barcode")
    private String barcode;

    @Column(name = "category_id")
    private Long categoryId;

    @Column(name = "unit_measure_id")
    private Long unitMeasureId;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "status_id")
    private Long statusId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}