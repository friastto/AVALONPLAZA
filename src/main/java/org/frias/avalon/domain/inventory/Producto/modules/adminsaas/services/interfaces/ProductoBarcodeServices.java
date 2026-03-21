package org.frias.avalon.domain.inventory.Producto.modules.adminsaas.services.interfaces;

import org.frias.avalon.domain.inventory.Producto.modules.adminsaas.dtos.ProductBarcodeRequestDto;

public interface ProductoBarcodeServices {

    boolean addBarcode(ProductBarcodeRequestDto productBarcodeRequestDto);
}
