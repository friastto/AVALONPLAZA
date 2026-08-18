package org.frias.avalon.domain.outlet.application.dto.response;

import org.frias.avalon.domain.masterdata.application.dto.response.StatusResponseDto;
import org.frias.avalon.domain.outlet.application.dto.LocationDto;
import org.frias.avalon.domain.product.application.dto.response.ProductResponse;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO for representing the full details of an Outlet, including its product catalog.
 */
public record OutletDetailResponse(
        Long id,
        Long companyId,
        String code,
        String name,
        String address,
        String phone,
        String nit,
        LocationDto location,
        StatusResponseDto statusResponseDto,
        Boolean deliveryEnabled,
        BigDecimal deliveryFee,
        List<ProductResponse> catalog
) {
}