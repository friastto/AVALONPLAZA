package org.frias.avalon.domain.inventory.Producto.modules.admincompany.services.interfaces;


import org.frias.avalon.domain.inventory.Producto.modules.admincompany.dto.BarcodeRequestDto;
import org.frias.avalon.domain.inventory.Producto.modules.admincompany.dto.BarcodeResponseNewDto;
import org.frias.avalon.domain.inventory.Producto.modules.adminsaas.dtos.ProductResponseDto;
import org.frias.avalon.domain.inventory.Producto.modules.adminsaas.entities.ProductCompany;

public interface ProductoCompanyService {

    BarcodeResponseNewDto addBarcode(BarcodeRequestDto barcodeRequestDto);
    ProductCompany save(ProductCompany productCompany);

    ProductResponseDto addSaasProductToCompanyCatalog(Long idAvalonProduct);
    ProductCompany searchProductCompanyByIdProductAvalonProduct(Long idProductAvalon);
}
