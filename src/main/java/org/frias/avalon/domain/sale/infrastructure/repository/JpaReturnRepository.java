package org.frias.avalon.domain.sale.infrastructure.repository;

import org.frias.avalon.domain.sale.infrastructure.entity.ReturnEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface JpaReturnRepository extends JpaRepository<ReturnEntity, Long> {

    Optional<ReturnEntity> findByReturnCode(UUID returnCode);

    List<ReturnEntity> findByOriginalSaleId(Long originalSaleId);

    Page<ReturnEntity> findByOutletId(Long outletId, Pageable pageable);
}
