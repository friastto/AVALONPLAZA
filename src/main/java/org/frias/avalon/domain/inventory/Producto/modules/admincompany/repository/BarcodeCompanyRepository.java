package org.frias.avalon.domain.inventory.Producto.modules.admincompany.repository;

import org.frias.avalon.domain.inventory.Producto.modules.adminsaas.entities.ProductBarcode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BarcodeCompanyRepository extends JpaRepository<ProductBarcode, Long> {
    Optional<ProductBarcode> existsByBarcode(String barcode);
}
