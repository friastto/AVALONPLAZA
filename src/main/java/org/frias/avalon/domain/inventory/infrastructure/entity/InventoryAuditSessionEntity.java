package org.frias.avalon.domain.inventory.infrastructure.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "inventory_audit_session")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class InventoryAuditSessionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "outlet_id", nullable = false)
    private Long outletId;

    @Column(name = "company_id")
    private Long companyId;

    @Column(name = "status", nullable = false, length = 30)
    private String status;

    @Column(name = "opened_by_user_id", nullable = false)
    private Long openedByUserId;

    @Column(name = "opened_by_name", length = 150)
    private String openedByName;

    @Column(name = "closed_by_user_id")
    private Long closedByUserId;

    @Column(name = "closed_by_name", length = 150)
    private String closedByName;

    @Column(name = "notes", length = 500)
    private String notes;

    @Column(name = "total_software_value", precision = 15, scale = 2)
    private BigDecimal totalSoftwareValue;

    @Column(name = "total_physical_value", precision = 15, scale = 2)
    private BigDecimal totalPhysicalValue;

    @Column(name = "total_difference_value", precision = 15, scale = 2)
    private BigDecimal totalDifferenceValue;

    @Column(name = "opened_at", nullable = false)
    private LocalDateTime openedAt;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    @OneToMany(mappedBy = "auditSession", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<InventoryAuditItemEntity> items = new ArrayList<>();

    @OneToMany(mappedBy = "auditSession", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<InventoryAuditSignatureEntity> signatures = new ArrayList<>();
}
