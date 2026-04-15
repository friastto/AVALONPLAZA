package org.frias.avalon.domain.product.presentation.controller.admin;

import org.frias.avalon.core.exeptions.ApiResponse;
import org.frias.avalon.domain.product.application.dto.company.ProductRequestCreate;
import org.frias.avalon.domain.product.application.dto.company.ProductResponseDto;
import org.frias.avalon.domain.product.application.dto.saas.ProductAvalonRequestDataDto;
import org.frias.avalon.domain.product.application.dto.saas.ProductAvalonResponseDto;
import org.frias.avalon.domain.product.application.usecase.saas.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/avalon/admin/saas/products")
public class SaasProductController {


    private final CreateProductUseCase createProductUseCase;
    private final SearchProductByIdUseCase searchProductByIdUseCase;
    private final DeleteProductByIdUseCase deleteProductByIdUseCase;
    private final UpdateProductUseCase updateProductUseCase;
    private final GetAllProductsUseCase getAllProductByIdUseCase;
    private final DisableProductByIdUseCase disableProductByIdUseCase;
    private final NearbyProductByNameUseCase nearbyProductByNameUseCase;

    public SaasProductController(CreateProductUseCase createProductUseCase, SearchProductByIdUseCase searchProductByIdUseCase, DeleteProductByIdUseCase deleteProductByIdUseCase, UpdateProductUseCase updateProductUseCase, GetAllProductsUseCase getAllProductByIdUseCase, DisableProductByIdUseCase disableProductByIdUseCase, NearbyProductByNameUseCase nearbyProductByNameUseCase) {
        this.createProductUseCase = createProductUseCase;
        this.searchProductByIdUseCase = searchProductByIdUseCase;
        this.deleteProductByIdUseCase = deleteProductByIdUseCase;
        this.updateProductUseCase = updateProductUseCase;
        this.getAllProductByIdUseCase = getAllProductByIdUseCase;
        this.disableProductByIdUseCase = disableProductByIdUseCase;
        this.nearbyProductByNameUseCase = nearbyProductByNameUseCase;
    }


    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ProductAvalonResponseDto>> createProduct(
            @RequestPart("data") ProductAvalonRequestDataDto data,
            @RequestPart("image") MultipartFile image) {


        ProductAvalonResponseDto response = createProductUseCase.execute(data, image);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(201, "Producto creado con exito", response));
    }

    @GetMapping("/search/v1/{id}")
    public ResponseEntity<ApiResponse<ProductAvalonResponseDto>> searchById(
            @PathVariable Long id
    ) {

        ProductAvalonResponseDto response = searchProductByIdUseCase.execute(id);

        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse<>(200, "producto encontrado", response));
    }

    @GetMapping("/nearby")
    public ResponseEntity<ApiResponse<List<ProductAvalonResponseDto>>> searchById(
            @RequestParam("name") String name
    ) {

        List<ProductAvalonResponseDto> response = nearbyProductByNameUseCase.execute(name);

        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse<>(200, "producto encontrado", response));
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<ProductAvalonResponseDto>>> getAll() {

        List<ProductAvalonResponseDto> response = getAllProductByIdUseCase.execute();

        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse<>(200, "producto encontrado", response));
    }
    @PutMapping("/Delete/{id}")
    public ResponseEntity<ApiResponse<ProductAvalonResponseDto>> deleteById(
            @PathVariable Long id
    ) {

        deleteProductByIdUseCase.execute(id);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(200, "Producto eliminado con exito", null));
    }
    @PutMapping("/disable/{id}")
    public ResponseEntity<ApiResponse<ProductAvalonResponseDto>> disableProductById(
            @PathVariable Long id
    ) {

        ProductAvalonResponseDto productoAvalon = disableProductByIdUseCase.execute(id);

        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse<>(200, "Producto inabilitado con exito", productoAvalon));
    }
    @PutMapping(value = "/update/{id}")
    public ResponseEntity<ApiResponse<ProductAvalonResponseDto>> updateProduct(
            @PathVariable Long id,
            @RequestBody ProductAvalonRequestDataDto data
            ) {


        ProductAvalonResponseDto response = updateProductUseCase.execute(id, data);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(200, "Producto actualizado con exito", response));
    }

}
