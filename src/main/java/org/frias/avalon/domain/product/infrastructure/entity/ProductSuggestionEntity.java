package org.frias.avalon.domain.product.infrastructure.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "product_suggestion_request", schema = "public")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ProductSuggestionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "outlet_id", nullable = false)
    private Long outletId;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(name = "requested_by", nullable = false)
    private Long requestedBy;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "barcode")
    private String barcode;

    @Column(name = "price", nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(name = "unit_measure_id", nullable = false)
    private Long unitMeasureId;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "image_urls", columnDefinition = "text[]")
    private List<String> imageUrls;

    @Column(name = "status", nullable = false)
    private String status; // PENDING, APPROVED, REJECTED

    @Column(name = "rejection_reason")
    private String rejectionReason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Version
    private Long version;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = "PENDING";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
