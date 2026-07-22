package org.frias.avalon.domain.sale.application.usecase.sale.returns;

import org.frias.avalon.domain.sale.application.dto.response.ReturnResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface FindReturnsUseCase {
    Optional<ReturnResponse> findByCode(UUID returnCode);
    Page<ReturnResponse> findByOutlet(Long outletId, Pageable pageable);
}
