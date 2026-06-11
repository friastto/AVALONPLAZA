package org.frias.avalon.domain.product.infraestructure.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;


@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"barcode", "outlet"})})

@NoArgsConstructor

@AllArgsConstructor
@Getter
@Setter
@Builder
public class Barcode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String barcode; // El código que lee el escáner

    @Column(nullable = false)
    private Long productOutlet; // A qué producto pertenece

    private String description; // EJ: "Código viejo" o "Empaque edición especial"

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