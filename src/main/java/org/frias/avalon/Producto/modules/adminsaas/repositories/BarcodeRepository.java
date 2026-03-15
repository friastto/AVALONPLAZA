package org.frias.avalon.Producto.modules.adminsaas.repositories;

import org.frias.avalon.Producto.modules.adminsaas.entities.ProductBarcode;
import org.frias.avalon.empresasucursal.tenant.config.TenantAware;
import org.springframework.data.jpa.repository.JpaRepository;

@TenantAware
public interface BarcodeRepository extends JpaRepository<ProductBarcode, Long> {

  Boolean existsByBarcode(String s);
}

