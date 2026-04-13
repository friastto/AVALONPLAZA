package org.frias.avalon.domain.inventory.Producto.modules.admincompany.services.interfaces;


import org.frias.avalon.domain.inventory.Producto.modules.admincompany.dto.BarcodeRequestDto;
import org.frias.avalon.domain.product.application.dto.company.ProductRequestCreate;
import org.frias.avalon.domain.product.application.dto.company.ProductResponseDto;
import org.frias.avalon.domain.product.domain.entity.ProductCompany;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ProductoCompanyService {

    Boolean addBarcode(BarcodeRequestDto barcodeRequestDto);
    ProductCompany save(ProductCompany productCompany);

    ProductResponseDto addSaasProductToCompanyCatalog(Long idAvalonProduct);
    ProductCompany searchProductCompanyByIdProductAvalonProduct(Long idProductAvalon);

    List<ProductResponseDto> getAll();

    ProductResponseDto update(Long id, ProductRequestCreate productRequestCreate, MultipartFile imgUrl);
}
