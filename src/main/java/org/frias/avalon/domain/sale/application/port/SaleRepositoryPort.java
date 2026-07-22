package org.frias.avalon.domain.sale.application.port;

import org.frias.avalon.domain.sale.domain.SaleDomain;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface SaleRepositoryPort {

    SaleDomain save(SaleDomain sale);

    Optional<SaleDomain> findByCode(UUID code);

    Optional<SaleDomain> findById(Long id);

    Page<SaleDomain> findByOutletId(Long outletId, Pageable pageable);
}
