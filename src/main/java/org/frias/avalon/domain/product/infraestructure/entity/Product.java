package org.frias.avalon.domain.product.infraestructure.entity;


import jakarta.persistence.*;
import lombok.*;
import org.frias.avalon.domain.masterdata.infraestructure.persistence.entity.MasterData;

import java.time.LocalDateTime;

/*
@Entity
@Table(name = "products")

 */
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

    private MasterData category;

    private MasterData unitMeasure;

    private String imageUrl;

    private MasterData status;

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