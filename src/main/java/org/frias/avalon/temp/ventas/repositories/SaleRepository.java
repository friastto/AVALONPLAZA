package org.frias.avalon.temp.ventas.repositories;

import org.frias.avalon.temp.ventas.entities.Sale;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SaleRepository extends JpaRepository<Sale, Long> {
}
