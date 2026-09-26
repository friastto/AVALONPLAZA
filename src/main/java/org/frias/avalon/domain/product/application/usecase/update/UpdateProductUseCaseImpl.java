package org.frias.avalon.domain.product.application.usecase.update;

import org.frias.avalon.core.exeptions.BusinessException;
import org.frias.avalon.core.exeptions.DomainValidationException;
import org.frias.avalon.core.exeptions.ResourceNotFoundException;
import org.frias.avalon.core.permissions.CurrentUserProviderPort;
import org.frias.avalon.core.tenant.TenantContext;
import org.frias.avalon.domain.masterdata.domain.model.MasterRoot;
import org.frias.avalon.domain.masterdata.domain.model.MasterTree;
import org.frias.avalon.domain.masterdata.domain.service.MasterTreeProvider;
import org.frias.avalon.domain.outlet.infraestructure.entities.Outlet;
import org.frias.avalon.domain.outlet.infraestructure.repository.JpaOutletRepository;
import org.frias.avalon.domain.product.application.dto.request.ProductUpdateRequest;
import org.frias.avalon.domain.product.application.dto.response.ProductResponse;
import org.frias.avalon.domain.product.application.port.ProductOutletRepositoryPort;
import org.frias.avalon.domain.product.application.service.QuantityParserService;
import org.frias.avalon.domain.product.domain.ProductDomain;
import org.frias.avalon.domain.product.domain.service.UnitConversionService;
import org.frias.avalon.domain.product.infraestructure.mapper.ProductOutletMapper;
import org.frias.avalon.domain.product.presentation.ProductWebSocketPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Use case to update details of an existing product.
 * Supports multi-tenant isolation, 3-level RBAC dynamic resolution and reactive WebSocket notification.
 */
@Service
public class UpdateProductUseCaseImpl implements UpdateProductUseCase {

    private final ProductOutletRepositoryPort productOutletRepositoryPort;
    private final MasterTreeProvider masterTreeProvider;
    private final QuantityParserService quantityParserService;
    private final UnitConversionService unitConversionService;
    private final ProductOutletMapper productOutletMapper;
    private final CurrentUserProviderPort currentUserProvider;
    private final ProductWebSocketPublisher productWebSocketPublisher;
    private final JpaOutletRepository jpaOutletRepository;
    private final TransactionTemplate transactionTemplate;

    public UpdateProductUseCaseImpl(
            ProductOutletRepositoryPort productOutletRepositoryPort,
            MasterTreeProvider masterTreeProvider,
            QuantityParserService quantityParserService,
            UnitConversionService unitConversionService,
            ProductOutletMapper productOutletMapper,
            CurrentUserProviderPort currentUserProvider,
            ProductWebSocketPublisher productWebSocketPublisher,
            JpaOutletRepository jpaOutletRepository,
            PlatformTransactionManager transactionManager) {
        this.productOutletRepositoryPort = productOutletRepositoryPort;
        this.masterTreeProvider = masterTreeProvider;
        this.quantityParserService = quantityParserService;
        this.unitConversionService = unitConversionService;
        this.productOutletMapper = productOutletMapper;
        this.currentUserProvider = currentUserProvider;
        this.productWebSocketPublisher = productWebSocketPublisher;
        this.jpaOutletRepository = jpaOutletRepository;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        this.transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    @Override
    public ProductResponse execute(Long productId, ProductUpdateRequest request) {
        Long previousOutletId = TenantContext.getTenantOutletId();
        Long previousTenantId = TenantContext.getTenantId();

        try {
            boolean isGlobalAdmin = currentUserProvider.hasRole("ROLE_ADMIN") || currentUserProvider.hasRole("ROLE_ADMINTI");
            boolean isCompanyAdmin = currentUserProvider.hasRole("ROLE_GERGEN");
            Long userOutletId = currentUserProvider.getCurrentOutletId();
            Long userTenantId = currentUserProvider.getCurrentTenantId();

            if (!isGlobalAdmin && !isCompanyAdmin && userOutletId == null) {
                throw new BusinessException("No se detecto una tienda asociada en el contexto del empleado actual.");
            }

            // Case 1: Standard store employee with explicit outlet context
            if (userOutletId != null) {
                Outlet employeeOutlet = jpaOutletRepository.findById(userOutletId).orElse(null);
                if (employeeOutlet != null && employeeOutlet.getCompanyId() != null) {
                    TenantContext.setTenantId(employeeOutlet.getCompanyId());
                }
                TenantContext.setTenantOutletId(userOutletId);

                return updateInCurrentTenant(productId, request, userOutletId, false);
            }

            // Case 2: Global Admin or Company Admin without fixed outletId
            // Try searching through available stores
            List<Outlet> outlets = jpaOutletRepository.findAll();
            for (Outlet outlet : outlets) {
                if (isCompanyAdmin && userTenantId != null && !userTenantId.equals(outlet.getCompanyId())) {
                    continue;
                }

                if (outlet.getCompanyId() != null) {
                    TenantContext.setTenantId(outlet.getCompanyId());
                }
                TenantContext.setTenantOutletId(outlet.getId());

                try {
                    ProductResponse response = updateInCurrentTenant(productId, request, outlet.getId(), true);
                    if (response != null) {
                        return response;
                    }
                } catch (ResourceNotFoundException ignored) {
                    // Try next outlet
                }
            }

            throw new ResourceNotFoundException("El producto con ID " + productId + " no existe.");
        } finally {
            TenantContext.setTenantId(previousTenantId);
            TenantContext.setTenantOutletId(previousOutletId);
        }
    }

    private ProductResponse updateInCurrentTenant(Long productId, ProductUpdateRequest request, Long targetOutletId, boolean allowNotFound) {
        return transactionTemplate.execute(status -> {
            Optional<ProductDomain> productOpt = productOutletRepositoryPort.findById(productId);
            if (productOpt.isEmpty()) {
                if (allowNotFound) {
                    throw new ResourceNotFoundException("El producto no existe en esta tienda.");
                }
                throw new ResourceNotFoundException("El producto con ID " + productId + " no existe.");
            }

            ProductDomain productDomain = productOpt.get();

            if (targetOutletId != null && !targetOutletId.equals(productDomain.getOutletId())) {
                throw new BusinessException("Acceso denegado: No tienes permisos para actualizar productos de otra tienda.");
            }

            BigDecimal validQuantity = quantityParserService.parseAndValidate(request.stockQuantity());

            MasterTree masterTree = masterTreeProvider.getTree();
            MasterRoot unitNode = masterTree.getById(request.stockUnitId());

            if (unitNode == null) {
                throw new DomainValidationException("El ID de la unidad de medida proporcionado no existe.");
            }
            if (!masterTree.isChildOf(unitNode, "UNIT")) {
                throw new DomainValidationException("El ID proporcionado no es una unidad de medida valida.");
            }

            String unitCode = unitNode.getShortName();
            Integer stockInBaseUnits = unitConversionService.convertToSmallestUnit(validQuantity, unitCode);

            productDomain.updateDetails(
                    request.name(),
                    request.description(),
                    stockInBaseUnits,
                    request.stockUnitId(),
                    request.imageUrl(),
                    request.price()
            );

            ProductDomain updatedProduct = productOutletRepositoryPort.save(productDomain);
            ProductResponse response = productOutletMapper.toResponse(updatedProduct);

            if (productWebSocketPublisher != null && response != null) {
                productWebSocketPublisher.broadcastProductStockChanged(response.outletId(), response);
            }

            return response;
        });
    }
}
