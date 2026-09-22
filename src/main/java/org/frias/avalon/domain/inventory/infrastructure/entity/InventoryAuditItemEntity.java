package org.frias.avalon.domain.inventory.infrastructure.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "inventory_audit_item")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class InventoryAuditItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "audit_session_id", nullable = false)
    private InventoryAuditSessionEntity auditSession;

    @Column(name = "product_outlet_id", nullable = false)
    private Long productOutletId;

    @Column(name = "product_name", nullable = false, length = 255)
    private String productName;

    @Column(name = "barcode", length = 100)
    private String barcode;

    @Column(name = "unit_measure", nullable = false, length = 20)
    private String unitMeasure;

    @Column(name = "unit_price", nullable = false, precision = 15, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "software_stock", nullable = false, precision = 12, scale = 3)
    private BigDecimal softwareStock;

    @Column(name = "software_value", nullable = false, precision = 15, scale = 2)
    private BigDecimal softwareValue;

    @Column(name = "physical_stock", precision = 12, scale = 3)
    private BigDecimal physicalStock;

    @Column(name = "physical_value", precision = 15, scale = 2)
    private BigDecimal physicalValue;

    @Column(name = "difference_stock", precision = 12, scale = 3)
    private BigDecimal differenceStock;

    @Column(name = "difference_value", precision = 15, scale = 2)
    private BigDecimal differenceValue;

    @Column(name = "discrepancy_reason", length = 255)
    private String discrepancyReason;

    @Column(name = "counted_by_user_id")
    private Long countedByUserId;

    @Column(name = "counted_by_name", length = 150)
    private String countedByName;

    @Column(name = "status", nullable = false, length = 30)
    private String status;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
