package org.frias.avalon.domain.product.infraestructure;

import org.frias.avalon.core.tenant.config.TenantAware;
import org.frias.avalon.domain.product.domain.entity.ProductBarcode;
import org.springframework.data.jpa.repository.JpaRepository;

@TenantAware
public interface BarcodeRepository extends JpaRepository<ProductBarcode, Long> {

  Boolean existsByBarcode(String s);
}

