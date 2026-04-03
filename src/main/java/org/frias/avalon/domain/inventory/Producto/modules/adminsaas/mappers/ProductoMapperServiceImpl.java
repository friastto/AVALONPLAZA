package org.frias.avalon.domain.inventory.Producto.modules.adminsaas.mappers;

import org.frias.avalon.domain.inventory.Producto.modules.admincompany.services.interfaces.ProductoCompanyService;
import org.frias.avalon.domain.inventory.Producto.modules.adminsaas.dtos.ProductResponseDto;
import org.frias.avalon.domain.inventory.Producto.modules.adminsaas.entities.Product;
import org.frias.avalon.domain.inventory.Producto.modules.adminsaas.entities.ProductBarcode;
import org.frias.avalon.domain.inventory.Producto.modules.adminsaas.entities.ProductCompany;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductoMapperServiceImpl implements ProductoMapperService, ProductoCompanyMapperService {
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

    @Override
    public ProductResponseDto toDto(ProductCompany p) {
// 1. Manejo seguro de la lista de barcodes
        String barcodesFormatted = Optional.ofNullable(p.getBarcodes())
                .map(list -> list.stream()
                        .map(ProductBarcode::getBarcode)
                        .filter(bc -> bc != null && !bc.isBlank())
                        .collect(Collectors.joining(", ")))
                .orElse(""); // Si la lista es nula o el stream termina vacío, devuelve ""

        return   new ProductResponseDto(
                p.getId(),

              barcodesFormatted,

                p.getCustomName() != null ? p.getCustomName() : p.getProduct().getName(),
                p.getCustomDescription() != null ? p.getCustomDescription(): p.getProduct().getDescription(),
                p.getCustomPrice() != null ? p.getCustomPrice() :new BigDecimal("0.00") ,
                new BigDecimal("0.00"),
                new BigDecimal("0.00"),
                p.getProduct().getCategory().getFullName(),
                p.getProduct().getUnit().getShortName(),
                "0 : UND or 0.00 : MASA",
                p.getCustomImageUrl() != null ? p.getCustomImageUrl():p.getProduct().getImageUrl()
        );



    }
}
