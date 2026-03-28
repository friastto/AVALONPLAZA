package org.frias.avalon.domain.inventory.Producto.modules.adminsaas.repositories;

import org.frias.avalon.core.tenant.config.TenantAware;
import org.frias.avalon.domain.inventory.Producto.modules.adminsaas.entities.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@TenantAware
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {


    Optional<Product> findByName(String sku);


}
