package org.frias.avalon.domain.product.application.mapper;

import org.frias.avalon.domain.product.application.dto.company.ProductResponseDto;
import org.frias.avalon.domain.product.domain.entity.ProductOutlet;

public interface ProductoOutletMapperService {

    ProductResponseDto toDto(ProductOutlet productOutlet);
}
