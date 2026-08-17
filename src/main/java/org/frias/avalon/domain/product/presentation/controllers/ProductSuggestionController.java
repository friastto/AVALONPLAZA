package org.frias.avalon.domain.product.presentation.controllers;

import lombok.RequiredArgsConstructor;
import org.frias.avalon.core.exeptions.ApiResponse;
import org.frias.avalon.domain.masterdata.domain.repository.MasterDataRepositoryPort;
import org.frias.avalon.domain.product.infrastructure.entity.ProductCompanyEntity;
import org.frias.avalon.domain.product.infrastructure.repository.JpaProductCompanyRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador simplificado para gestionar las sugerencias y promociones de productos
 * en el Nivel 2 (public.product_company) utilizando directamente los estados de revision (RVW, APR, REC).
 */
@RestController
@RequestMapping("/avalon/products/suggestions")
@RequiredArgsConstructor
public class ProductSuggestionController {

    private final JpaProductCompanyRepository productCompanyRepository;
    private final MasterDataRepositoryPort masterDataRepositoryPort;

    @PostMapping
    public ResponseEntity<ApiResponse<ProductCompanyEntity>> createSuggestion(@RequestBody ProductCompanyEntity productCompany) {
        Long rvwStatusId = masterDataRepositoryPort.getIdByCode("RVW"); // EN_REVISION
        productCompany.setStatusId(rvwStatusId != null ? rvwStatusId : 1L);
        ProductCompanyEntity saved = productCompanyRepository.save(productCompany);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(HttpStatus.CREATED.value(), "Sugerencia registrada en el catalogo corporativo exitosamente", saved));
    }

    @GetMapping("/company/{companyId}")
    public ResponseEntity<ApiResponse<List<ProductCompanyEntity>>> getPendingSuggestionsByCompany(@PathVariable Long companyId) {
        List<ProductCompanyEntity> companyProducts = productCompanyRepository.findByCompanyId(companyId);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Productos de la compania obtenidos exitosamente", companyProducts));
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<ProductCompanyEntity>> approveSuggestion(@PathVariable Long id) {
        ProductCompanyEntity productCompany = productCompanyRepository.findById(id).orElse(null);
        if (productCompany == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(HttpStatus.NOT_FOUND.value(), "Producto corporativo no encontrado", null));
        }
        Long aprStatusId = masterDataRepositoryPort.getIdByCode("APR"); // APROBADO
        if (aprStatusId == null) {
            aprStatusId = masterDataRepositoryPort.getIdByCode("ACT"); // ACTIVO
        }
        productCompany.setStatusId(aprStatusId);
        ProductCompanyEntity updated = productCompanyRepository.save(productCompany);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Producto aprobado corporativamente y propagado a tiendas", updated));
    }
}
