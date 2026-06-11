package org.frias.avalon.domain.product.infraestructure.repository;

import org.frias.avalon.domain.product.infraestructure.entity.ProductOutlet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface JpaProductOutletRepository extends JpaRepository<ProductOutlet, Long>, JpaSpecificationExecutor<ProductOutlet> {

}
