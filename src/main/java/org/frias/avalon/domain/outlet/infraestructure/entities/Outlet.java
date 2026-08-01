package org.frias.avalon.domain.outlet.infraestructure.entities;


import jakarta.persistence.*;
import lombok.*;
import org.locationtech.jts.geom.Point;

import java.time.LocalDateTime;
import java.util.UUID;
import java.math.BigDecimal;

@Entity
@Table
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Builder
public class Outlet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    private String nit;

    private String name;

    private String address;

    private String phone;

    /*@Column(nullable = false)
    private boolean isMain = false;
     */
    private Long statusId;

    @Column(columnDefinition = "Geometry(Point, 4326)")
    private Point location;

    // Scalar coupling to Company domain — no @ManyToOne cross-domain JPA reference (Clean Architecture)
    @Column(name = "company_id")
    private Long companyId;


    private BigDecimal cashThresholdAmount;


    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        codeGenerator();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void codeGenerator() {
        if (this.code == null || this.code.isBlank()) {
            this.code = generateCode();
        }
    }

    private String generateCode() {
        // Podrías usar un UUID corto o una lógica más personalizada
        return "Oult-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
