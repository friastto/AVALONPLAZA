package org.frias.avalon.Producto.modules.adminsaas.entities;

import jakarta.persistence.*;
import lombok.*;
import org.frias.avalon.empresasucursal.empresa.entities.Company;
import org.frias.avalon.empresasucursal.tenant.config.TenantEntity;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

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

    private String description; // Ej: "Código viejo" o "Empaque edición especial"

    @ManyToOne
    private Product product; // A qué producto pertenece

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_product_id")
    private ProductCompany companyProduct;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;


}