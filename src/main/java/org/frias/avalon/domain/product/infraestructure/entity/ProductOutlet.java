package org.frias.avalon.domain.product.infraestructure.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ProductOutlet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String localName;

    private String localDescription;

    @Column(nullable = false)
    private Integer stock;

    @Column(nullable = false)
    private Long unitMeasureId; // Añadido para persistir la unidad de medida

    private String localImageUrl;

    @Column(nullable = false)
    private BigDecimal localPrice;

    private Long outletId;

    private Long statusId;

    private LocalDateTime createdAt;

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
