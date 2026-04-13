package org.frias.avalon.domain.product.infraestructure.repository;

import org.frias.avalon.domain.product.domain.entity.ProductOutlet;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaProductOutletRepository extends JpaRepository<ProductOutlet, Long> {
}
