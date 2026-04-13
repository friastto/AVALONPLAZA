package org.frias.avalon.domain.product.infraestructure.repository;


import org.frias.avalon.domain.product.domain.entity.ProductCompany;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaProductCompanyRepository extends JpaRepository<ProductCompany, Long> {

}
