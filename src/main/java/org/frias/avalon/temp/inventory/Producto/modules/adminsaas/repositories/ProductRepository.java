package org.frias.avalon.temp.inventory.Producto.modules.adminsaas.repositories;

import org.frias.avalon.temp.inventory.Producto.modules.adminsaas.entities.Product;
import org.frias.avalon.temp.empresasucursal.tenant.config.TenantAware;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@TenantAware
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {


    Optional<Product> findByName(String sku);


}
