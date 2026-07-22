package org.frias.avalon.domain.sale.infrastructure.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Tabla: product_returns
 * Registra cada devolución/cambio realizado en POS.
 */
@Entity
@Table(name = "product_returns")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReturnEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private UUID returnCode;

    /** FK a la venta original */
    @Column(nullable = false)
    private Long originalSaleId;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalRefundAmount;

    /** DEFECTO | INCORRECTO | OTRO */
    @Column(nullable = false, length = 20)
    private String reason;

    /** REEMBOLSO | NOTA_CREDITO | CAMBIO */
    @Column(nullable = false, length = 20)
    private String resolutionType;

    /** FK a master_data (estado DEV = procesada) */
    @Column(nullable = false)
    private Long statusId;

    @Column(nullable = false)
    private Long employeeId;

    @Column(nullable = false)
    private Long outletId;

    @Column(nullable = false)
    private Long clientId;

    @Column(nullable = false)
    private LocalDateTime returnDate;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "returnEntity", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ReturnItemEntity> items = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.returnCode == null) this.returnCode = UUID.randomUUID();
        if (this.returnDate == null) this.returnDate = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
