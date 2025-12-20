package org.frias.avalon.ventas.repositories;

import org.frias.avalon.ventas.entities.Sale;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SaleRepository extends JpaRepository<Sale, Long> {
}
