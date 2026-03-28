package org.frias.avalon.domain.inventory.Producto.modules.adminoulet.services.interfaces;

import org.frias.avalon.domain.outlet.dtos.request.OutletMap;
import org.frias.avalon.domain.outlet.dtos.response.OutletsWhitProductMap;
import org.frias.avalon.domain.inventory.Producto.modules.adminoulet.ProductOutletResponseDto;
import org.frias.avalon.domain.inventory.Producto.modules.adminsaas.entities.ProductOutlet;

import java.util.List;

public interface ProductOutletService {


    List<ProductOutletResponseDto> getAllProductCatalog();
    List<ProductOutletResponseDto> getProductCatalogToOutlet(Long id);
    List<OutletsWhitProductMap> getOutletProductByNameProduct(OutletMap requestMap);
    void addAll(List<ProductOutlet> productOutletList);
}
