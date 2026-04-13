package org.frias.avalon.domain.product.application.services.interfaces;

import jakarta.validation.constraints.NotBlank;
import org.frias.avalon.domain.product.application.dto.company.ProductResponseDto;
import org.frias.avalon.domain.product.domain.entity.Product;
import org.frias.avalon.domain.product.domain.repository.ProductRepository;
import org.springframework.web.multipart.MultipartFile;

public interface ProductAvalonService {


    Product createProduct(
            String name,
            String description,
            Long categoryId,
            Long uniMeasureId,
            MultipartFile image
    );
}
