package org.frias.avalon.domain.inventory.Producto.modules.adminoulet.mapper;

import org.frias.avalon.domain.inventory.Producto.modules.adminoulet.ProductOutletResponseDto;
import org.frias.avalon.domain.product.domain.entity.ProductBarcode;
import org.frias.avalon.domain.product.domain.entity.ProductOutlet;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class ProductOutletMapperServiceImpl implements ProductOutletMapperService {


    @Override
    public ProductOutletResponseDto toDto(ProductOutlet productOutlet) {




        return new ProductOutletResponseDto(
                productOutlet.getId(),
                productOutlet.getLocalName() != null ? productOutlet.getLocalName() : productOutlet.getCompanyProduct().getProduct().getName(),
                productOutlet.getLocalDescription() != null ? productOutlet.getLocalDescription() : productOutlet.getCompanyProduct().getProduct().getDescription(),
                productOutlet.getLocalImageUrl() != null ? productOutlet.getLocalImageUrl() : productOutlet.getCompanyProduct().getProduct().getImageUrl(),
                productOutlet.getCompanyProduct().getProduct().getCategory().getFullName(),
                productOutlet.getCompanyProduct().getProduct().getUnit().getFullName(),
                productOutlet.getLocalPrice() != null && productOutlet.getLocalPrice().compareTo(BigDecimal.ZERO) > 0 ? productOutlet.getLocalPrice(): productOutlet.getCompanyProduct().getCustomPrice(),
                productOutlet.getStock(),
                // Transformamos la lista de objetos Barcode a lista de Strings
                productOutlet.getCompanyProduct().getBarcodes()
                        .stream()
                        .map(ProductBarcode::getBarcode) // Cambia 'getCode' por el name real de tu atributo
                        .toList()
        );



    }
}
