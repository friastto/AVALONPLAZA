package org.frias.avalon.domain.inventory.Producto.modules.adminoulet.services.interfaces;

import org.frias.avalon.domain.inventory.Producto.modules.adminoulet.ProductOutletResponseDto;
import org.frias.avalon.domain.inventory.Producto.modules.adminsaas.entities.ProductOutlet;
import org.frias.avalon.empresasucursal.sucursal.dtos.OutletsRequestMap;
import org.frias.avalon.empresasucursal.sucursal.dtos.OutletsWhitProductResponseMap;

import java.util.List;

public interface ProductOutletService {


    List<ProductOutletResponseDto> getAllProductCatalog();
    List<ProductOutletResponseDto> getProductCatalogToOutlet(Long id);
    List<OutletsWhitProductResponseMap> getOutletProductByNameProduct(OutletsRequestMap requestMap);
    void addAll(List<ProductOutlet> productOutletList);
}
