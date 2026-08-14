package org.frias.avalon.domain.inventory.application.usecase;

import org.frias.avalon.domain.inventory.application.dto.KardexResponseDto;

import java.util.List;

/**
 * Input Port / Interface for querying Kardex ledger history.
 */
public interface GetKardexUseCase {

    /**
     * Retrieves Kardex entries by product outlet ID.
     */
    List<KardexResponseDto> findByProductOutletId(Long productOutletId);

    /**
     * Retrieves Kardex entries by store outlet ID.
     */
    List<KardexResponseDto> findByOutletId(Long outletId);
}
