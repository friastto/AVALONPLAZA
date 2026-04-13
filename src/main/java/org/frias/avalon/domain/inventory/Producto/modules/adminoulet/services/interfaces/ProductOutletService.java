package org.frias.avalon.domain.inventory.Producto.modules.adminoulet.services.interfaces;

import org.frias.avalon.domain.inventory.Producto.modules.adminoulet.ProductOutletResponseDto;
import org.frias.avalon.domain.product.application.dto.company.ProductResponseDto;
import org.frias.avalon.domain.product.domain.entity.ProductOutlet;
import org.frias.avalon.domain.outlet.dtos.request.OutletMap;
import org.frias.avalon.domain.outlet.dtos.response.OutletWithCatalogProductResponse;
import org.frias.avalon.domain.outlet.dtos.response.OutletsWhitProductMap;

import java.util.List;

public interface ProductOutletService {


    List<ProductOutletResponseDto> getAllProductCatalog();
    List<ProductOutletResponseDto> getProductCatalogToOutlet(Long id);
    List<OutletsWhitProductMap> getOutletProductByNameProduct(OutletMap requestMap);
    void addAll(List<ProductOutlet> productOutletList);
    ProductResponseDto SearchProduct(Long id);

    OutletWithCatalogProductResponse getOutletWithCatalogProduct(Long id);
}
