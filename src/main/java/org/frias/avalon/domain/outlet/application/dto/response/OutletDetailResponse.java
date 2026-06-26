package org.frias.avalon.domain.outlet.application.dto.response;

import org.frias.avalon.domain.masterdata.application.dto.response.StatusResponseDto;
import org.frias.avalon.domain.outlet.application.dto.LocationDto;
import org.frias.avalon.domain.product.application.dto.response.ProductResponse;

import java.util.List;

/**
 * DTO for representing the full details of an Outlet, including its product catalog.
 *
 * @param id                The unique identifier of the outlet.
 * @param code              The internal code of the outlet.
 * @param name              The name of the outlet.
 * @param address           The physical address of the outlet.
 * @param phone             The contact phone number.
 * @param nit               The tax identification number.
 * @param location          The geographical location (latitude/longitude).
 * @param statusResponseDto The status of the outlet.
 * @param catalog           The list of products available at the outlet.
 */
public record OutletDetailResponse(
        Long id,
        String code,
        String name,
        String address,
        String phone,
        String nit,
        LocationDto location,
        StatusResponseDto statusResponseDto,
        List<ProductResponse> catalog
) {
}