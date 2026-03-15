package org.frias.avalon.Producto.modules.adminoulet.services.interfaces;

import org.frias.avalon.Producto.modules.adminoulet.ProductOutletResponseDto;
import org.frias.avalon.Producto.modules.adminsaas.entities.ProductOutlet;

import java.util.List;

public interface ProductOutletService {


    List<ProductOutletResponseDto> getAllProductCatalog();
}
