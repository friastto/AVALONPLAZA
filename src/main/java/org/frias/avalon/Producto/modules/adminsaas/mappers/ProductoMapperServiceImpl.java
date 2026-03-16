package org.frias.avalon.Producto.modules.adminsaas.mappers;

import org.frias.avalon.Producto.modules.adminsaas.dtos.ProductResponseDto;
import org.frias.avalon.Producto.modules.adminsaas.entities.Product;
import org.frias.avalon.Producto.modules.adminsaas.entities.ProductBarcode;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class ProductoMapperServiceImpl implements ProductoMapperService {
    @Override
    public ProductResponseDto toDto(Product p) {




        return new ProductResponseDto(
                p.getId(),
               "N/A",
                p.getName(),
                p.getDescription(),
                new BigDecimal("0.0"),
                new BigDecimal("0.0"),
                new BigDecimal("0.0"),
                p.getCategory().getFullName(),
                p.getUnit().getShortName(),
                "0 : UND or 0.0 : MASA",
                p.getImageUrl()
        );

    }
}
