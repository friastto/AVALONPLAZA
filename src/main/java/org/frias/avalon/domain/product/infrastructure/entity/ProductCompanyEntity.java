package org.frias.avalon.domain.product.infrastructure.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Level 2 — Company product catalog.
 * Represents a product selected by a company from the global Avalon catalog (Level 1).
 * Sets the corporate reference price (customPrice) that all its stores inherit by default.
 *
 * Hierarchy:
 *   ProductEntity (L1, public.product)
 *     -> ProductCompanyEntity (L2, public.product_company)
 *       -> ProductOutlet (L3, store_{id}.product_outlet)
 */
@Entity
@Table(name = "product_company", schema = "public")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ProductCompanyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** FK to the global product (Level 1) — scalar coupling only */
    @Column(name = "product_id", nullable = false)
    private Long productId;

    /** FK to the company that selected this product — scalar coupling only */
    @Column(name = "company_id", nullable = false)
    private Long companyId;

    /**
     * Corporate reference price set by the Company Manager.
     * All stores inherit this price unless they override with localPrice.
     */
    @Column(name = "custom_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal customPrice;

    /**
     * Custom product image URL set by the company (Level 2 override).
     * Overrides the global base image from Level 1.
     */
    @Column(name = "custom_image_url")
    private String customImageUrl;

    /** Enabled/disabled status of this product for the company */
    @Column(name = "status_id")
    private Long statusId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Version
    private Long version;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
