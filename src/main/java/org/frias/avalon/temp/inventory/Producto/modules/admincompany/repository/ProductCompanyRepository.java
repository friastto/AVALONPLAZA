package org.frias.avalon.temp.inventory.Producto.modules.admincompany.repository;

import org.frias.avalon.temp.inventory.Producto.modules.adminsaas.entities.ProductCompany;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductCompanyRepository extends JpaRepository<ProductCompany, Long> {
    boolean existsByProductIdAndCompanyId(Long productId, Long companyId);
}
