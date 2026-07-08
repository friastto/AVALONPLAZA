package org.frias.avalon.domain.sale.infrastructure.repository;

import org.frias.avalon.domain.sale.infrastructure.entity.SaleEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface JpaSaleRepository extends JpaRepository<SaleEntity, Long> {

    Optional<SaleEntity> findBySaleCode(UUID saleCode);

    Page<SaleEntity> findByOutletId(Long outletId, Pageable pageable);
}
