package org.frias.avalon.domain.product.presentation.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.frias.avalon.core.exeptions.ApiResponse;
import org.frias.avalon.core.exeptions.BusinessException;
import org.frias.avalon.core.exeptions.ResourceNotFoundException;
import org.frias.avalon.core.permissions.CurrentUserProviderPort;
import org.frias.avalon.domain.company.domain.model.CompanyDomain;
import org.frias.avalon.domain.company.domain.port.CompanyRepositoryPort;
import org.frias.avalon.domain.masterdata.application.dto.response.MasterRefDto;
import org.frias.avalon.domain.masterdata.domain.model.MasterRoot;
import org.frias.avalon.domain.masterdata.domain.model.MasterTree;
import org.frias.avalon.domain.masterdata.domain.service.MasterTreeProvider;
import org.frias.avalon.domain.outlet.domain.model.OutletDomain;
import org.frias.avalon.domain.outlet.domain.port.OutletRepositoryPort;
import org.frias.avalon.domain.product.application.dto.request.ProductSuggestionRequestDto;
import org.frias.avalon.domain.product.application.dto.response.ProductSuggestionResponseDto;
import org.frias.avalon.domain.product.application.usecase.propagate.PropagateCompanyProductToOutletsUseCase;
import org.frias.avalon.domain.product.infraestructure.entity.Product;
import org.frias.avalon.domain.product.infraestructure.repository.JpaGlobalProductRepository;
import org.frias.avalon.domain.product.infrastructure.entity.ProductCompanyEntity;
import org.frias.avalon.domain.product.infrastructure.repository.JpaProductCompanyRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

/**
 * Controlador REST para gestionar sugerencias de productos entre Tiendas (Nivel 3)
 * y la Gerencia de la Empresa (Nivel 2), con aprobacion y propagacion automatica a tiendas.
 */
@RestController
@RequestMapping("/avalon/products/suggestions")
@RequiredArgsConstructor
public class ProductSuggestionController {

    private final JpaProductCompanyRepository productCompanyRepository;
    private final JpaGlobalProductRepository globalProductRepository;
    private final OutletRepositoryPort outletRepositoryPort;
    private final CompanyRepositoryPort companyRepositoryPort;
    private final MasterTreeProvider masterTreeProvider;
    private final PropagateCompanyProductToOutletsUseCase propagateCompanyProductToOutletsUseCase;
    private final CurrentUserProviderPort currentUserProvider;

    @PostMapping
    public ResponseEntity<ApiResponse<ProductSuggestionResponseDto>> createSuggestion(@Valid @RequestBody ProductSuggestionRequestDto request) {
        MasterTree tree = masterTreeProvider.getTree();
        MasterRoot rvwNode = tree.getByCode("RVW");
        Long rvwStatusId = rvwNode != null ? rvwNode.getId() : 1L;

        Long targetCompanyId = request.companyId();
        if (targetCompanyId == null && request.outletId() != null) {
            targetCompanyId = outletRepositoryPort.findById(request.outletId())
                    .map(OutletDomain::getCompanyId)
                    .orElse(null);
        }
        if (targetCompanyId == null) {
            targetCompanyId = currentUserProvider.getCurrentTenantId();
        }
        if (targetCompanyId == null) {
            throw new BusinessException("No fue posible determinar la compania a la que pertenece la sugerencia.");
        }

        Product product;
        if (request.productId() != null) {
            product = globalProductRepository.findById(request.productId())
                    .orElseThrow(() -> new ResourceNotFoundException("Producto global con ID " + request.productId() + " no encontrado"));
        } else {
            // Verificar si ya existe por codigo de barras
            if (request.barcode() != null && !request.barcode().isBlank()) {
                Optional<Product> existingBarcode = globalProductRepository.findByBarcode(request.barcode());
                if (existingBarcode.isPresent()) {
                    product = existingBarcode.get();
                } else {
                    product = createNewGlobalProduct(request, rvwStatusId);
                }
            } else {
                product = createNewGlobalProduct(request, rvwStatusId);
            }
        }

        Optional<ProductCompanyEntity> existingCompProd = productCompanyRepository
                .findByProductIdAndCompanyId(product.getId(), targetCompanyId);

        ProductCompanyEntity productCompany;
        if (existingCompProd.isPresent()) {
            productCompany = existingCompProd.get();
            productCompany.setCustomPrice(request.suggestedPrice());
            if (request.imageUrl() != null && !request.imageUrl().isBlank()) {
                productCompany.setCustomImageUrl(request.imageUrl());
            }
            productCompany.setStatusId(rvwStatusId);
        } else {
            productCompany = ProductCompanyEntity.builder()
                    .productId(product.getId())
                    .companyId(targetCompanyId)
                    .customPrice(request.suggestedPrice())
                    .customImageUrl(request.imageUrl())
                    .statusId(rvwStatusId)
                    .build();
        }

        ProductCompanyEntity saved = productCompanyRepository.save(productCompany);

        ProductSuggestionResponseDto responseDto = toResponseDto(saved, tree, request.outletId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(HttpStatus.CREATED.value(), "Sugerencia registrada exitosamente para revision de gerencia", responseDto));
    }

    @GetMapping("/company/{companyId}")
    public ResponseEntity<ApiResponse<List<ProductSuggestionResponseDto>>> getPendingSuggestionsByCompany(
            @PathVariable Long companyId,
            @RequestParam(required = false) String statusCode) {
        MasterTree tree = masterTreeProvider.getTree();
        List<ProductCompanyEntity> companyProducts = productCompanyRepository.findByCompanyId(companyId);

        if (statusCode != null && !statusCode.isBlank()) {
            MasterRoot filterStatus = tree.getByCode(statusCode.trim().toUpperCase());
            if (filterStatus != null) {
                Long filterStatusId = filterStatus.getId();
                companyProducts = companyProducts.stream()
                        .filter(cp -> filterStatusId.equals(cp.getStatusId()))
                        .toList();
            }
        }

        List<ProductSuggestionResponseDto> responseList = companyProducts.stream()
                .map(cp -> toResponseDto(cp, tree, null))
                .toList();

        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Productos y sugerencias de la compania obtenidos exitosamente", responseList));
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<ProductSuggestionResponseDto>> approveSuggestion(@PathVariable Long id) {
        ProductCompanyEntity productCompany = productCompanyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sugerencia de producto con ID " + id + " no encontrada"));

        MasterTree tree = masterTreeProvider.getTree();
        MasterRoot actStatus = tree.getByCode("ACT");
        Long activeStatusId = actStatus != null ? actStatus.getId() : 1L;

        productCompany.setStatusId(activeStatusId);
        ProductCompanyEntity updated = productCompanyRepository.save(productCompany);

        // Propagacion automatica en cascada a todas las tiendas de la empresa
        int propagatedCount = propagateCompanyProductToOutletsUseCase.execute(updated.getId());

        ProductSuggestionResponseDto responseDto = toResponseDto(updated, tree, null);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(),
                "Producto aprobado corporativamente y propagado a " + propagatedCount + " tiendas exitosamente", responseDto));
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<ProductSuggestionResponseDto>> rejectSuggestion(@PathVariable Long id) {
        ProductCompanyEntity productCompany = productCompanyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sugerencia de producto con ID " + id + " no encontrada"));

        MasterTree tree = masterTreeProvider.getTree();
        MasterRoot recStatus = tree.getByCode("REC");
        Long rejectedStatusId = recStatus != null ? recStatus.getId() : null;

        productCompany.setStatusId(rejectedStatusId);
        ProductCompanyEntity updated = productCompanyRepository.save(productCompany);

        ProductSuggestionResponseDto responseDto = toResponseDto(updated, tree, null);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Sugerencia de producto rechazada", responseDto));
    }

    private Product createNewGlobalProduct(ProductSuggestionRequestDto request, Long statusId) {
        Product newProduct = Product.builder()
                .name(request.name() != null ? request.name().trim() : "Producto Sugerido")
                .description(request.description())
                .barcode(request.barcode())
                .categoryId(request.categoryId())
                .unitMeasureId(request.unitMeasureId())
                .imageUrl(request.imageUrl())
                .statusId(statusId)
                .build();
        return globalProductRepository.save(newProduct);
    }

    private ProductSuggestionResponseDto toResponseDto(ProductCompanyEntity pc, MasterTree tree, Long outletId) {
        Product product = globalProductRepository.findById(pc.getProductId()).orElse(null);
        String prodName = product != null ? product.getName() : "Producto #" + pc.getProductId();
        String prodDesc = product != null ? product.getDescription() : null;
        String barcode = product != null ? product.getBarcode() : null;
        MasterRefDto category = (product != null && product.getCategoryId() != null)
                ? MasterRefDto.from(tree.getById(product.getCategoryId())) : null;
        MasterRefDto unitMeasure = (product != null && product.getUnitMeasureId() != null)
                ? MasterRefDto.from(tree.getById(product.getUnitMeasureId())) : null;
        String imageUrl = pc.getCustomImageUrl() != null && !pc.getCustomImageUrl().isBlank()
                ? pc.getCustomImageUrl() : (product != null ? product.getImageUrl() : null);

        String companyName = companyRepositoryPort.findById(pc.getCompanyId())
                .map(CompanyDomain::name)
                .orElse("Compania " + pc.getCompanyId());

        String outletName = null;
        if (outletId != null) {
            outletName = outletRepositoryPort.findById(outletId)
                    .map(OutletDomain::getName)
                    .orElse(null);
        }

        MasterRefDto status = pc.getStatusId() != null
                ? MasterRefDto.from(tree.getById(pc.getStatusId())) : null;

        return new ProductSuggestionResponseDto(
                pc.getId(),
                pc.getProductId(),
                prodName,
                prodDesc,
                barcode,
                category,
                unitMeasure,
                pc.getCustomPrice(),
                imageUrl,
                pc.getCompanyId(),
                companyName,
                outletId,
                outletName,
                status,
                pc.getCreatedAt()
        );
    }
}
