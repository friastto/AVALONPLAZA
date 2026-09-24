package org.frias.avalon.domain.product.presentation.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import java.util.List;
import org.frias.avalon.core.exeptions.ApiResponse;
import org.frias.avalon.domain.product.application.dto.request.ChangeStatusRequest;
import org.frias.avalon.domain.product.application.dto.request.LinkBarcodeRequest;
import org.frias.avalon.domain.product.application.dto.request.NearbyStoresByProductRequestDto;
import org.frias.avalon.domain.product.application.dto.request.ProductNewDataRequest;
import org.frias.avalon.domain.product.application.dto.request.ProductUpdateRequest;
import org.frias.avalon.domain.product.application.dto.response.NearbyStoreProductResponseDto;
import org.frias.avalon.domain.product.application.dto.response.ProductResponse;
import org.frias.avalon.domain.product.application.usecase.changestatus.ChangeProductStatusUseCase;
import org.frias.avalon.domain.product.application.usecase.create.CreateProductOutletUseCase;
import org.frias.avalon.domain.product.application.usecase.find.FindNearbyProductSuggestionsUseCase;
import org.frias.avalon.domain.product.application.usecase.find.FindNearbyStoresByProductUseCase;
import org.frias.avalon.domain.product.application.usecase.find.FindProductByBarcodeUseCase;
import org.frias.avalon.domain.product.application.usecase.find.FindProductCatalogByOutletUseCase;
import org.frias.avalon.domain.product.application.usecase.find.FindProductCatalogUseCase;
import org.frias.avalon.domain.product.application.usecase.find.FindProductByIdUseCase;
import org.frias.avalon.domain.product.application.usecase.linkbarcode.LinkBarcodeToProductUseCase;
import org.frias.avalon.domain.product.application.usecase.update.UpdateProductUseCase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST para gestionar productos.
 */
@RestController
@RequestMapping("/avalon/products")
@RequiredArgsConstructor
public class ProductOutletController {

    private final CreateProductOutletUseCase createProductOutletUseCase;
    private final LinkBarcodeToProductUseCase linkBarcodeToProductUseCase;
    private final FindProductCatalogUseCase findProductCatalogUseCase;
    private final FindProductCatalogByOutletUseCase findProductCatalogByOutletUseCase;
    private final FindProductByBarcodeUseCase findProductByBarcodeUseCase;
    private final FindProductByIdUseCase findProductByIdUseCase;
    private final UpdateProductUseCase updateProductUseCase;
    private final ChangeProductStatusUseCase changeProductStatusUseCase;
    private final FindNearbyProductSuggestionsUseCase findNearbyProductSuggestionsUseCase;
    private final FindNearbyStoresByProductUseCase findNearbyStoresByProductUseCase;

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(@Valid @RequestBody ProductNewDataRequest request) {

        System.out.println(request.toString());

        ProductResponse response = createProductOutletUseCase.execute(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(HttpStatus.CREATED.value(), "Producto creado exitosamente", response));
    }

    @PostMapping("/link-barcode")
    public ResponseEntity<ApiResponse<Void>> linkBarcode(@Valid @RequestBody LinkBarcodeRequest request) {
        linkBarcodeToProductUseCase.execute(request);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(HttpStatus.OK.value(), "Código de barras vinculado exitosamente al producto", null));
    }

    @GetMapping("/catalog")
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> getProductCatalog(@RequestParam(required = false) String name, @PageableDefault(size = 10) Pageable pageable) {
        Page<ProductResponse> catalogPage = findProductCatalogUseCase.execute(name, pageable);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(HttpStatus.OK.value(), "Catálogo de productos obtenido exitosamente", catalogPage));
    }

    @GetMapping("/catalog/outlet/{outletId}")
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> getProductCatalogByOutlet(
            @PathVariable Long outletId,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long categoryId,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<ProductResponse> catalogPage = findProductCatalogByOutletUseCase.execute(outletId, name, categoryId, pageable);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(HttpStatus.OK.value(), "Catalogo de productos de la tienda obtenido exitosamente", catalogPage));
    }

    @GetMapping("/barcode/{barcode}")
    public ResponseEntity<ApiResponse<ProductResponse>> getProductByBarcode(@PathVariable String barcode) {
        ProductResponse product = findProductByBarcodeUseCase.execute(barcode);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(HttpStatus.OK.value(), "Producto encontrado exitosamente", product));
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ApiResponse<ProductResponse>> getProductById(
            @PathVariable Long productId,
            @RequestParam(required = false) Long outletId) {
        ProductResponse product = findProductByIdUseCase.execute(productId, outletId);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(HttpStatus.OK.value(), "Producto encontrado exitosamente", product));
    }

    @PutMapping("/{productId}")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(@PathVariable Long productId, @Valid @RequestBody ProductUpdateRequest request) {
        ProductResponse updatedProduct = updateProductUseCase.execute(productId, request);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(HttpStatus.OK.value(), "Producto actualizado exitosamente", updatedProduct));
    }

    /**
     * Endpoint para cambiar el estado de un producto.
     *
     * @param productId ID del producto a actualizar.
     * @param request   DTO con el nuevo ID de estado.
     * @return ResponseEntity con un ApiResponse que contiene el producto con su nuevo estado.
     */
    @PatchMapping("/{productId}/status")
    public ResponseEntity<ApiResponse<ProductResponse>> changeProductStatus(
            @PathVariable Long productId,
            @Valid @RequestBody ChangeStatusRequest request) {
        
        ProductResponse updatedProduct = changeProductStatusUseCase.execute(productId, request);
        
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse<>(
                        HttpStatus.OK.value(),
                        "Estado del producto actualizado exitosamente",
                        updatedProduct
                ));
    }

    @GetMapping("/nearby/suggestions")
    public ResponseEntity<ApiResponse<List<String>>> getNearbyProductSuggestions(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(required = false, defaultValue = "2000") int radius,
            @RequestParam String query) {
        List<String> suggestions = findNearbyProductSuggestionsUseCase.execute(latitude, longitude, radius, query);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(HttpStatus.OK.value(), "Sugerencias de productos obtenidas exitosamente", suggestions));
    }

    @PostMapping("/nearby/stores")
    public ResponseEntity<ApiResponse<List<NearbyStoreProductResponseDto>>> getNearbyStoresByProduct(
            @Valid @RequestBody NearbyStoresByProductRequestDto request) {
        List<NearbyStoreProductResponseDto> stores = findNearbyStoresByProductUseCase.execute(request);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(HttpStatus.OK.value(), "Tiendas cercanas con el producto obtenidas exitosamente", stores));
    }
}
