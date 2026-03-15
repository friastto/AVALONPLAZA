package org.frias.avalon.Producto.modules.adminoulet.mapper;

import org.frias.avalon.Producto.modules.adminoulet.ProductOutletResponseDto;
import org.frias.avalon.Producto.modules.adminsaas.entities.ProductBarcode;
import org.frias.avalon.Producto.modules.adminsaas.entities.ProductOutlet;
import org.springframework.stereotype.Component;

@Component
public class ProductOutletMapperServiceImpl implements ProductOutletMapperService {


    @Override
    public ProductOutletResponseDto toDto(ProductOutlet productOutlet) {




        return new ProductOutletResponseDto(
                productOutlet.getId(),
                productOutlet.getCustomName() != null ? productOutlet.getCustomName() : productOutlet.getCompanyProduct().getProduct().getName(),
                productOutlet.getCustomDescription() != null ? productOutlet.getCustomDescription() : productOutlet.getCompanyProduct().getProduct().getDescription(),
                productOutlet.getLocalImageUrl() != null ? productOutlet.getLocalImageUrl() : productOutlet.getCompanyProduct().getProduct().getImageUrl(),
                productOutlet.getCompanyProduct().getProduct().getCategory().getFullName(),
                productOutlet.getCompanyProduct().getProduct().getUnit().getFullName(),
                productOutlet.getLocalPrice(),
                productOutlet.getStock(),
                // Transformamos la lista de objetos Barcode a lista de Strings
                productOutlet.getCompanyProduct().getBarcodes()
                        .stream()
                        .map(ProductBarcode::getBarcode) // Cambia 'getCode' por el nombre real de tu atributo
                        .toList()
        );



    }
}
