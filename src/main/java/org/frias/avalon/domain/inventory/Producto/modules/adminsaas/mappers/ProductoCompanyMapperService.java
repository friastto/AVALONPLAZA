package org.frias.avalon.domain.inventory.Producto.modules.adminsaas.mappers;

import org.frias.avalon.domain.inventory.Producto.modules.adminsaas.dtos.ProductResponseDto;
import org.frias.avalon.domain.inventory.Producto.modules.adminsaas.entities.ProductCompany;

public interface ProductoCompanyMapperService {

    ProductResponseDto toDto(ProductCompany productCompany);
}
