package org.frias.avalon.Producto.modules.adminsaas.services.interfaces;


import org.frias.avalon.Producto.modules.adminsaas.dtos.ProductRequestCreate;
import org.frias.avalon.Producto.modules.adminsaas.dtos.ProductResponseDto;
import org.frias.avalon.Producto.modules.adminsaas.entities.Product;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

public interface ProductoService {
    Product searchById(Long id);
    BigDecimal calculatePrice(Long productId, String identity);

    ProductResponseDto save(ProductRequestCreate request, MultipartFile imgUrl);


    ProductResponseDto findByCodeBar(String codeBar);

    ProductResponseDto searchByName(String name);

    List<ProductResponseDto> findAll();
}
