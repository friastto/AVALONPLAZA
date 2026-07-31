package org.frias.avalon.domain.product.infraestructure.repository;

import org.frias.avalon.domain.product.infraestructure.entity.ProductOutlet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface JpaProductOutletRepository extends JpaRepository<ProductOutlet, Long>, JpaSpecificationExecutor<ProductOutlet> {

    @org.springframework.data.jpa.repository.Query(value = "SELECT * FROM public.product_outlet p WHERE p.outlet_id = :outletId AND (:name IS NULL OR LOWER(p.local_name) LIKE LOWER(CONCAT('%', :name, '%')))", nativeQuery = true)
    Page<ProductOutlet> findFromPublicSchema(@org.springframework.data.repository.query.Param("outletId") Long outletId, @org.springframework.data.repository.query.Param("name") String name, Pageable pageable);
}
