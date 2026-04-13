package org.frias.avalon.domain.product.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.frias.avalon.core.tenant.config.TenantEntity;
import org.frias.avalon.domain.company.entities.Company;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

import java.time.LocalDateTime;

@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"barcode", "company_id"})})
@FilterDef(name = "tenantFilter", parameters = @ParamDef(name = "empresaId", type = Long.class))
@Filter(name = "tenantFilter", condition = "company_id = :empresaId")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ProductBarcode extends TenantEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String barcode; // El código que lee el escáner

    private String description; // EJ: "Código viejo" o "Empaque edición especial"

    @ManyToOne
    private Product product; // A qué producto pertenece

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_product_id")
    private ProductCompany companyProduct;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;

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