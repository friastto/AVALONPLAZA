package org.frias.avalon.sales.ventas.repositories;

import org.frias.avalon.sales.ventas.entities.Sale;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SaleRepository extends JpaRepository<Sale, Long> {
}
