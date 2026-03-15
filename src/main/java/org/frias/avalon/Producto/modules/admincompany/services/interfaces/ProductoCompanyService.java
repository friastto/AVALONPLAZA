package org.frias.avalon.Producto.modules.admincompany.services.interfaces;


import org.frias.avalon.Producto.modules.admincompany.services.dto.BarcodeRequestDto;
import org.frias.avalon.Producto.modules.admincompany.services.dto.BarcodeResponseNewDto;

public interface ProductoCompanyService {

    BarcodeResponseNewDto addBarcode(BarcodeRequestDto barcodeRequestDto);
}
