package org.frias.avalon.domain.product.application.usecase.create;

import lombok.RequiredArgsConstructor;
import org.frias.avalon.core.exeptions.BusinessException;
import org.frias.avalon.core.exeptions.DomainValidationException;
import org.frias.avalon.core.permissions.CurrentUserProviderPort;
import org.frias.avalon.domain.masterdata.domain.model.MasterRoot;
import org.frias.avalon.domain.masterdata.domain.model.MasterTree;
import org.frias.avalon.domain.masterdata.domain.repository.MasterDataRepositoryPort;
import org.frias.avalon.domain.masterdata.domain.service.MasterTreeProvider;
import org.frias.avalon.domain.product.application.dto.request.ProductNewDataRequest;
import org.frias.avalon.domain.product.application.dto.response.ProductResponse;
import org.frias.avalon.domain.product.application.port.ProductOutletRepositoryPort;
import org.frias.avalon.domain.product.application.service.QuantityParserService;
import org.frias.avalon.domain.product.domain.BarcodeDomain;
import org.frias.avalon.domain.product.domain.ProductDomain;
import org.frias.avalon.domain.product.domain.exceptions.ProductAlreadyExistsException;
import org.frias.avalon.domain.product.domain.repository.BarcodeRepositoryPort;
import org.frias.avalon.domain.product.domain.service.UnitConversionService;
import org.frias.avalon.domain.product.infraestructure.mapper.ProductOutletMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Caso de uso para registrar un producto en una tienda.
 * Valida la consistencia de los datos, la existencia del código de barras,
 * y aplica reglas estrictas de aislamiento de tienda (Tenant Isolation).
 */
@Service
@RequiredArgsConstructor
public class CreateProductOutletUseCaseImpl implements CreateProductOutletUseCase {

    private final ProductOutletRepositoryPort productOutletRepositoryPort;
    private final MasterDataRepositoryPort masterDataRepositoryPort;
    private final ProductOutletMapper productOutletMapper;
    private final UnitConversionService unitConversionService;
    private final MasterTreeProvider masterTreeProvider;
    private final QuantityParserService quantityParserService;
    private final BarcodeRepositoryPort barcodeRepositoryPort;
    private final CurrentUserProviderPort currentUserProvider;

    @Override
    @Transactional
    public ProductResponse execute(ProductNewDataRequest request) {

        // --- 0. Validar Encapsulación de Tienda (Tenant Isolation) ---
        boolean isSystemAdmin = currentUserProvider.hasRole("ROLE_ADMIN") || currentUserProvider.hasRole("ROLE_ADMINTI");
        if (!isSystemAdmin) {
            Long tenantOutletId = currentUserProvider.getCurrentOutletId();
            if (tenantOutletId == null) {
                throw new BusinessException("No se detectó una tienda asociada en el contexto del empleado actual.");
            }
            if (!tenantOutletId.equals(request.outletId())) {
                throw new BusinessException("Acceso denegado: No tienes permisos para registrar productos en otra tienda.");
            }
        }

        String barcode = request.barCode();
        // Validación de existencia por código de barras
        if (barcode != null && !barcode.trim().isEmpty()) {
            Optional<BarcodeDomain> existingBarcode = barcodeRepositoryPort.findByCode(request.barCode());
            if (existingBarcode.isPresent()) {
                throw new ProductAlreadyExistsException("A product with barcode '" + request.barCode() + "' already exists.");
            }
        }

        // 1. Obtener dependencias (ID del estado activo)
        Long activeStatusId = masterDataRepositoryPort.getIdByCode("ACT");
        if (activeStatusId == null) {
            throw new IllegalStateException("Active status ('ACT') not found in MasterData.");
        }

        // 2. Parsear y validar la cantidad usando el Application Service
        BigDecimal validQuantity = quantityParserService.parseAndValidate(request.stockQuantity());

        // 3. Validar la unidad de medida contra el MasterData
        MasterTree masterTree = masterTreeProvider.getTree();

        MasterRoot unitNode = masterTree.getById(request.stockUnitId());

        if (unitNode == null) {
            throw new DomainValidationException("The provided stock unit ID does not exist.");
        }
        if (!masterTree.isChildOf(unitNode, "UNIT")) {
            throw new DomainValidationException("The provided ID is not a valid unit of measurement.");
        }

        // 4. Usar el Domain Service para convertir la cantidad a la unidad base
        String unitCode = unitNode.getShortName();
        Integer stockInBaseUnits = unitConversionService.convertToSmallestUnit(validQuantity, unitCode);

        // 5. Delegar la creación del producto al Dominio
        ProductDomain newProductDomain = ProductDomain.create(
                request.name(),
                request.description(),
                stockInBaseUnits,
                request.stockUnitId(),
                request.imageUrl(),
                request.price(),
                request.outletId(),
                activeStatusId
        );

        // 6. Persistir el nuevo producto
        ProductDomain savedProductDomain = productOutletRepositoryPort.save(newProductDomain);

        // 7. Si se proporciona un código de barras, crearlo y persistirlo directamente
        if (request.barCode() != null && !request.barCode().trim().isEmpty()) {
            BarcodeDomain newBarcode = BarcodeDomain.create(
                    request.barCode(),
                    savedProductDomain.getId(),
                    "Código principal" // Descripción por defecto
            );
            barcodeRepositoryPort.save(newBarcode);
        }

        // 8. Mapear el resultado a un DTO de respuesta con el código de barras
        return productOutletMapper.toResponse(savedProductDomain, request.barCode());
    }
}
