package org.frias.avalon.domain.inventory.infrastructure.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Kardex immutable ledger entry.
 * Every stock change (INGESTION, SALE, MERMA, ADJUSTMENT_SURPLUS, TRANSFER) is recorded
 * as a new row — records are never updated or deleted, guaranteeing audit integrity.
 *
 * Mapped to: store_{outletId}.stock_movement
 */
@Entity
@Table(name = "stock_movement")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class StockMovementEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** FK to the product_outlet record this movement affects */
    @Column(name = "product_outlet_id", nullable = false)
    private Long productOutletId;

    /** Which outlet performed this movement */
    @Column(name = "outlet_id", nullable = false)
    private Long outletId;

    /**
     * Type of stock movement:
     * INGESTION       — physical reception of goods from supplier
     * SALE            — automatic deduction after POS sale
     * MERMA           — waste/damage write-off
     * ADJUSTMENT_SURPLUS — surplus found during shelf audit (3 -> 5 panelas)
     * TRANSFER        — inter-store transfer
     */
    @Column(name = "movement_type", nullable = false, length = 30)
    private String movementType;

    /** Stock level before the movement */
    @Column(name = "quantity_before", nullable = false)
    private Integer quantityBefore;

    /** Stock level after the movement */
    @Column(name = "quantity_after", nullable = false)
    private Integer quantityAfter;

    /** Net change: positive = entry, negative = exit */
    @Column(name = "quantity_delta", nullable = false)
    private Integer quantityDelta;

    /** Human-readable reason or justification for the movement */
    @Column(name = "reason", length = 500)
    private String reason;

    /** user_avalon.id of the operator who triggered the movement */
    @Column(name = "operator_id")
    private Long operatorId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
