package org.frias.avalon.domain.outlet.dtos.response;

import org.frias.avalon.domain.product.application.dto.company.ProductResponseDto;

import java.util.List;
import java.util.Map;

public record OutletWithCatalogProductResponse (
        Long id,
    String name,
    Map<String , List<ProductResponseDto>> matchingProducts
)
    {
}
