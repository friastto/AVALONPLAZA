package org.frias.avalon.domain.product.application.usecase.create;

import org.frias.avalon.core.exeptions.BusinessException;
import org.frias.avalon.core.exeptions.DomainValidationException;
import org.frias.avalon.core.exeptions.ResourceNotFoundException;
import org.frias.avalon.core.permissions.CurrentUserProviderPort;
import org.frias.avalon.core.tenant.TenantContext;
import org.frias.avalon.domain.masterdata.domain.model.MasterRoot;
import org.frias.avalon.domain.masterdata.domain.model.MasterTree;
import org.frias.avalon.domain.masterdata.domain.service.MasterTreeProvider;
import org.frias.avalon.domain.outlet.domain.model.OutletDomain;
import org.frias.avalon.domain.outlet.domain.port.OutletRepositoryPort;
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
import org.frias.avalon.domain.product.presentation.ProductWebSocketPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Caso de uso para registrar un producto en una tienda.
 * Valida la consistencia de los datos, la existencia del codigo de barras,
 * conmuta dinamicamente el esquema multi-tenant y aplica reglas de aislamiento (Tenant Isolation)
 * para Admin Global (Avalon), Gerente de Empresa (Company) y Empleado de Tienda.
 * Emite notificacion reactiva por WebSocket para sincronizacion instantanea de clientes.
 */
@Service
public class CreateProductOutletUseCaseImpl implements CreateProductOutletUseCase {

    private final ProductOutletRepositoryPort productOutletRepositoryPort;
    private final ProductOutletMapper productOutletMapper;
    private final UnitConversionService unitConversionService;
    private final MasterTreeProvider masterTreeProvider;
    private final QuantityParserService quantityParserService;
    private final BarcodeRepositoryPort barcodeRepositoryPort;
    private final CurrentUserProviderPort currentUserProvider;
    private final ProductWebSocketPublisher productWebSocketPublisher;
    private final OutletRepositoryPort outletRepositoryPort;
    private final TransactionTemplate transactionTemplate;

    public CreateProductOutletUseCaseImpl(
            ProductOutletRepositoryPort productOutletRepositoryPort,
            ProductOutletMapper productOutletMapper,
            UnitConversionService unitConversionService,
            MasterTreeProvider masterTreeProvider,
            QuantityParserService quantityParserService,
            BarcodeRepositoryPort barcodeRepositoryPort,
            CurrentUserProviderPort currentUserProvider,
            ProductWebSocketPublisher productWebSocketPublisher,
            OutletRepositoryPort outletRepositoryPort,
            PlatformTransactionManager transactionManager
    ) {
        this.productOutletRepositoryPort = productOutletRepositoryPort;
        this.productOutletMapper = productOutletMapper;
        this.unitConversionService = unitConversionService;
        this.masterTreeProvider = masterTreeProvider;
        this.quantityParserService = quantityParserService;
        this.barcodeRepositoryPort = barcodeRepositoryPort;
        this.currentUserProvider = currentUserProvider;
        this.productWebSocketPublisher = productWebSocketPublisher;
        this.outletRepositoryPort = outletRepositoryPort;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        this.transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    @Override
    public ProductResponse execute(ProductNewDataRequest request) {
        OutletDomain outlet = outletRepositoryPort.findById(request.outletId())
                .orElseThrow(() -> new ResourceNotFoundException("Tienda con ID " + request.outletId() + " no encontrada"));

        // --- 0. Validar Encapsulacion de Tienda y Permisos RBAC Multi-Nivel ---
        boolean isSystemAdmin = currentUserProvider.hasRole("ROLE_ADMIN")
                || currentUserProvider.hasRole("ROLE_ADMINTI")
                || currentUserProvider.hasRole("ROLE_ADMINSYS");
        boolean isCompanyAdmin = currentUserProvider.hasRole("ROLE_GERGEN");

        if (isSystemAdmin) {
            // Nivel 1 (Admin Global Avalon): Acceso irrestricto a cualquier tienda
        } else if (isCompanyAdmin) {
            // Nivel 2 (Gerencia Corporativa): Acceso delimitado a tiendas de su empresa
            Long userCompanyId = currentUserProvider.getCurrentTenantId();
            if (userCompanyId == null || !userCompanyId.equals(outlet.getCompanyId())) {
                throw new BusinessException("Acceso denegado: La tienda no pertenece a su empresa.");
            }
        } else {
            // Nivel 3 (Tienda Local): Acceso encapsulado a su tienda asignada
            Long tenantOutletId = currentUserProvider.getCurrentOutletId();
            if (tenantOutletId == null) {
                throw new BusinessException("No se detecto una tienda asociada en el contexto del empleado actual.");
            }
            if (!tenantOutletId.equals(request.outletId())) {
                throw new BusinessException("Acceso denegado: No tienes permisos para registrar productos en otra tienda.");
            }
        }

        Long previousTenantId = TenantContext.getTenantId();
        Long previousOutletId = TenantContext.getTenantOutletId();

        try {
            if (outlet.getCompanyId() != null) {
                TenantContext.setTenantId(outlet.getCompanyId());
            }
            TenantContext.setTenantOutletId(outlet.getId());

            ProductResponse response = transactionTemplate.execute(status -> doCreateProduct(request));

            // Notificar reactivamente a los clientes conectados por WebSocket
            if (productWebSocketPublisher != null && response != null) {
                productWebSocketPublisher.broadcastProductStockChanged(response.outletId(), response);
            }

            return response;
        } finally {
            TenantContext.setTenantId(previousTenantId);
            TenantContext.setTenantOutletId(previousOutletId);
        }
    }

    private ProductResponse doCreateProduct(ProductNewDataRequest request) {
        String barcode = request.barCode();
        // Validacion de existencia por codigo de barras dentro del esquema del tenant
        if (barcode != null && !barcode.trim().isEmpty()) {
            Optional<BarcodeDomain> existingBarcode = barcodeRepositoryPort.findByCode(request.barCode());
            if (existingBarcode.isPresent()) {
                throw new ProductAlreadyExistsException("A product with barcode '" + request.barCode() + "' already exists.");
            }
        }

        // 1. Obtener MasterTree y validar estado activo
        MasterTree masterTree = masterTreeProvider.getTree();
        MasterRoot activeStatusNode = masterTree.getByCode("ACT");
        if (activeStatusNode == null) {
            throw new IllegalStateException("Active status ('ACT') not found in MasterData.");
        }
        Long activeStatusId = activeStatusNode.getId();

        // 2. Parsear y validar la cantidad usando el Application Service
        BigDecimal validQuantity = quantityParserService.parseAndValidate(request.stockQuantity());

        // 3. Validar la unidad de medida contra el MasterData
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

        // 5. Delegar la creacion del producto al Dominio
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

        // 6. Persistir el nuevo producto en el esquema del tenant
        ProductDomain savedProductDomain = productOutletRepositoryPort.save(newProductDomain);

        // 7. Si se proporciona un codigo de barras, crearlo y persistirlo directamente
        if (request.barCode() != null && !request.barCode().trim().isEmpty()) {
            BarcodeDomain newBarcode = BarcodeDomain.create(
                    request.barCode(),
                    savedProductDomain.getId(),
                    "Codigo principal"
            );
            barcodeRepositoryPort.save(newBarcode);
        }

        // 8. Mapear el resultado a un DTO de respuesta con el codigo de barras
        return productOutletMapper.toResponse(savedProductDomain, request.barCode());
    }
}
