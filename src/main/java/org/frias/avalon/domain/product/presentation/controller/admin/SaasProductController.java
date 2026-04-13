package org.frias.avalon.domain.product.presentation.controller.admin;

import org.frias.avalon.core.exeptions.ApiResponse;
import org.frias.avalon.domain.product.application.dto.company.ProductRequestCreate;
import org.frias.avalon.domain.product.application.dto.company.ProductResponseDto;
import org.frias.avalon.domain.product.application.dto.saas.ProductAvalonRequestDataDto;
import org.frias.avalon.domain.product.application.dto.saas.ProductAvalonResponseDto;
import org.frias.avalon.domain.product.application.usecase.saas.CreateProductUseCase;
import org.frias.avalon.domain.product.application.usecase.saas.DeleteProductByIdUseCase;
import org.frias.avalon.domain.product.application.usecase.saas.SearchProductByIdUseCase;
import org.frias.avalon.domain.product.application.usecase.saas.UpdateProductUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/avalon/admin/saas/products")
public class SaasProductController {


    private final CreateProductUseCase createProductUseCase;
    private final SearchProductByIdUseCase searchProductByIdUseCase;
    private final DeleteProductByIdUseCase deleteProductByIdUseCase;
    private final UpdateProductUseCase updateProductUseCase;

    public SaasProductController(CreateProductUseCase createProductUseCase, SearchProductByIdUseCase searchProductByIdUseCase, DeleteProductByIdUseCase deleteProductByIdUseCase, UpdateProductUseCase updateProductUseCase) {
        this.createProductUseCase = createProductUseCase;
        this.searchProductByIdUseCase = searchProductByIdUseCase;
        this.deleteProductByIdUseCase = deleteProductByIdUseCase;
        this.updateProductUseCase = updateProductUseCase;
    }


    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ProductAvalonResponseDto>> createProduct(
            @RequestPart("data") ProductAvalonRequestDataDto data,
            @RequestPart("image") MultipartFile image) {


        ProductAvalonResponseDto response = createProductUseCase.execute(data, image);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(201, "Producto creado con exito", response));
    }

    @PostMapping("/search/v1/{id}")
    public ResponseEntity<ApiResponse<ProductAvalonResponseDto>> searchById(
            @PathVariable Long id
    ) {

        ProductAvalonResponseDto response = searchProductByIdUseCase.execute(id);

        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse<>(201, "producto encontrado", response));
    }

    @PutMapping("/Delete/{id}")
    public ResponseEntity<ApiResponse<ProductAvalonResponseDto>> deleteById(
            @PathVariable Long id
    ) {

        deleteProductByIdUseCase.execute(id);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(200, "Producto eliminado con exito", null));
    }

    @PostMapping(value = "/update", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ProductAvalonResponseDto>> updateProduct(
            @RequestPart("data") ProductRequestCreate data,
            @RequestPart("image") MultipartFile image) {


        ProductAvalonResponseDto response = updateProductUseCase.execute(data, image);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(201, "Producto actualizado con exito", response));
    }

}
