package org.frias.avalon.domain.product.application.services.interfaces;


import org.frias.avalon.domain.product.application.dto.company.ProductRequestCreate;
import org.frias.avalon.domain.product.application.dto.company.ProductResponseDto;
import org.frias.avalon.domain.product.application.dto.saas.ProductAvalonResponseDto;
import org.frias.avalon.domain.product.domain.entity.Product;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

public interface ProductoService {
    ProductAvalonResponseDto save(ProductRequestCreate request, MultipartFile imgUrl);

    Product searchById(Long id);

    BigDecimal calculatePrice(Long productId, String identity);

    ProductResponseDto findByCodeBar(String codeBar);

    ProductResponseDto searchByName(String name);

    List<ProductAvalonResponseDto> findAll();

    void updateImageUrl(Long productId, String finalFileName);

    void deleteById(Long id);

    ProductResponseDto save(
            String name,
            String desc,
            Long aLong,
            Long aLong1
    );
}
