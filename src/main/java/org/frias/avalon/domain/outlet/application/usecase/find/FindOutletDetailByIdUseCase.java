package org.frias.avalon.domain.outlet.application.usecase.find;

import org.frias.avalon.domain.outlet.application.dto.response.OutletDetailResponse;

/**
 * Input Port / Interface for retrieving outlet detail by ID including associated products.
 */
public interface FindOutletDetailByIdUseCase {

    /**
     * Executes the use case to find outlet detail.
     *
     * @param outletId outlet ID
     * @return OutletDetailResponse DTO
     */
    OutletDetailResponse execute(Long outletId);
}