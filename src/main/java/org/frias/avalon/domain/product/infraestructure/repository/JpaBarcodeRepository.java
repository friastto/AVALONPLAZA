package org.frias.avalon.domain.product.infraestructure.repository;

import org.frias.avalon.domain.product.infraestructure.entity.Barcode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JpaBarcodeRepository extends JpaRepository<Barcode,Long> {

    Optional<Barcode> findByBarcode(String barcode);
}
