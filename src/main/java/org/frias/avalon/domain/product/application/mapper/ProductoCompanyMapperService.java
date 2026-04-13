package org.frias.avalon.domain.product.application.mapper;

import org.frias.avalon.domain.product.application.dto.company.ProductResponseDto;
import org.frias.avalon.domain.product.application.dto.saas.ProductAvalonResponseDto;
import org.frias.avalon.domain.product.domain.entity.ProductCompany;

public interface ProductoCompanyMapperService {

    ProductResponseDto toDto(ProductCompany productCompany);
}
