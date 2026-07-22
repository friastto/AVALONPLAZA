package org.frias.avalon.domain.sale.application.port;

import org.frias.avalon.domain.sale.domain.ReturnDomain;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReturnRepositoryPort {

    ReturnDomain save(ReturnDomain returnDomain);

    Optional<ReturnDomain> findByCode(UUID returnCode);

    List<ReturnDomain> findByOriginalSaleId(Long originalSaleId);

    Page<ReturnDomain> findByOutletId(Long outletId, Pageable pageable);
}
