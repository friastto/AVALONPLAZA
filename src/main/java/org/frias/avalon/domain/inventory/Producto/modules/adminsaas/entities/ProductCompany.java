package org.frias.avalon.domain.inventory.Producto.modules.adminsaas.entities;

import jakarta.persistence.*;
import lombok.*;
import org.frias.avalon.empresasucursal.empresa.entities.Company;
import org.frias.avalon.empresasucursal.sucursal.entities.Outlet;
import org.frias.avalon.empresasucursal.tenant.config.TenantEntity;
import org.frias.avalon.maestra.entities.MasterData;
import org.frias.avalon.domain.inventory.promo.entities.Promotion;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "company_products", uniqueConstraints = {
@UniqueConstraint(columnNames = {"product_id", "company_id"}, name = "uk_product_per_company")
})
@FilterDef(name = "tenantFilter", parameters = @ParamDef(name = "empresaId", type = Long.class))
@Filter(name = "tenantFilter", condition = "company_id = :empresaId")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductCompany extends TenantEntity {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "product_id")
        private Product product; // Qué producto genérico es

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "company_id")
        private Company company;

        // Personalización a nivel empresa
        private String customName;
        private String customDescription;
        private String customImageUrl;

        @OneToMany(mappedBy = "productCompany", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
        private List<Promotion> promotions;



        // Sucursal que propuso el producto (si vino de una sucursal)
        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "outlet_id")
        private Outlet createdByOutlet;

        // PENDING / APPROVED / REJECTED
        @ManyToOne
        @JoinColumn(foreignKey = @ForeignKey(name = "fk_company_product_status"))
        private MasterData status;

        private String rejectionReason;

        // Barcodes propios de esta empresa para este producto
        @OneToMany(mappedBy = "companyProduct", cascade = CascadeType.ALL)
        private List<ProductBarcode> barcodes;

        private LocalDateTime createdAt;

        @PrePersist
        protected void onCreate() { this.createdAt = LocalDateTime.now(); }
}
