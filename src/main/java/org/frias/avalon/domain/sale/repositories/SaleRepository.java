package org.frias.avalon.domain.sale.repositories;

import org.frias.avalon.domain.sale.entities.Sale;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SaleRepository extends JpaRepository<Sale, Long> {
}
