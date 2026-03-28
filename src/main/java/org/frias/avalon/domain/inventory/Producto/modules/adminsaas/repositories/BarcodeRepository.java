package org.frias.avalon.domain.inventory.Producto.modules.adminsaas.repositories;

import org.frias.avalon.core.tenant.config.TenantAware;
import org.frias.avalon.domain.inventory.Producto.modules.adminsaas.entities.ProductBarcode;
import org.springframework.data.jpa.repository.JpaRepository;

@TenantAware
public interface BarcodeRepository extends JpaRepository<ProductBarcode, Long> {

  Boolean existsByBarcode(String s);
}

