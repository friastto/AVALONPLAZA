package org.frias.avalon.domain.product.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.frias.avalon.core.tenant.config.TenantEntity;
import org.frias.avalon.domain.company.domain.entities.Company;
import org.frias.avalon.domain.inventory.promo.entities.Promotion;
import org.frias.avalon.domain.outlet.entities.Outlet;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "outlet_products")
@FilterDef(name = "tenantFilter", parameters = @ParamDef(name = "empresaId", type = Long.class))
@Filter(name = "tenantFilter", condition = "company_id = :empresaId")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductOutlet extends TenantEntity {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;


        // ✅ Apunta a CompanyProduct, no a Product directamente
        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "company_product_id", nullable = false)
        private ProductCompany companyProduct;

        private String localName;

        private String localDescription;

        @Column(nullable = false)
        private Integer stock; // Stock específico de ESTA sucursal

        private String localImageUrl;

        @Column(nullable = false)
        private BigDecimal localPrice; // Por si en La Guajira es más caro que en Bogotá

        private boolean active; // Para que la sucursal pueda "ocultarlo" sin borrarlo

        @ManyToOne
        @JoinColumn(name = "outlet_id")
        private Outlet outlet; // A qué sucursal pertenece

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "company_id", nullable = false)
        private Company company;

        private LocalDateTime createdAt;

        private LocalDateTime updatedAt;

        @OneToMany(mappedBy = "productOutlet", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
        private List<Promotion> localPromotions;

        @PrePersist
        protected void onCreate() {
                this.createdAt = LocalDateTime.now();
        }

        @PreUpdate
        protected void onUpdate() {
                this.updatedAt = LocalDateTime.now();
        }

    // }
}
