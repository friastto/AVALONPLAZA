package org.frias.avalon.domain.product.application.usecase.changestatus;

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
import org.frias.avalon.domain.product.application.dto.request.ChangeStatusRequest;
import org.frias.avalon.domain.product.application.dto.response.ProductResponse;
import org.frias.avalon.domain.product.application.port.ProductOutletRepositoryPort;
import org.frias.avalon.domain.product.domain.ProductDomain;
import org.frias.avalon.domain.product.infraestructure.mapper.ProductOutletMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Optional;

/**
 * Use case to change operational status (Active/Inactive) of a product.
 * Supports multi-tenant isolation and 3-level RBAC dynamic resolution.
 */
@Service
public class ChangeProductStatusUseCaseImpl implements ChangeProductStatusUseCase {

    private final ProductOutletRepositoryPort productOutletRepositoryPort;
    private final MasterTreeProvider masterTreeProvider;
    private final ProductOutletMapper productOutletMapper;
    private final CurrentUserProviderPort currentUserProvider;
    private final JpaOutletRepository jpaOutletRepository;
    private final TransactionTemplate transactionTemplate;

    public ChangeProductStatusUseCaseImpl(
            ProductOutletRepositoryPort productOutletRepositoryPort,
            MasterTreeProvider masterTreeProvider,
            ProductOutletMapper productOutletMapper,
            CurrentUserProviderPort currentUserProvider,
            JpaOutletRepository jpaOutletRepository,
            PlatformTransactionManager transactionManager) {
        this.productOutletRepositoryPort = productOutletRepositoryPort;
        this.masterTreeProvider = masterTreeProvider;
        this.productOutletMapper = productOutletMapper;
        this.currentUserProvider = currentUserProvider;
        this.jpaOutletRepository = jpaOutletRepository;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        this.transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    @Override
    public ProductResponse execute(Long productId, ChangeStatusRequest request) {
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

                return changeStatusInCurrentTenant(productId, request, userOutletId, false);
            }

            // Case 2: Global Admin or Company Admin without fixed outletId
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
                    ProductResponse response = changeStatusInCurrentTenant(productId, request, outlet.getId(), true);
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

    private ProductResponse changeStatusInCurrentTenant(Long productId, ChangeStatusRequest request, Long targetOutletId, boolean allowNotFound) {
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
                throw new BusinessException("Acceso denegado: No tienes permisos para cambiar el estado de productos de otra tienda.");
            }

            MasterTree masterTree = masterTreeProvider.getTree();
            MasterRoot statusNode = masterTree.getById(request.newStatusId());

            if (statusNode == null) {
                throw new DomainValidationException("El ID de estado proporcionado no existe.");
            }
            if (!masterTree.isChildOf(statusNode, "STSGEN")) {
                throw new DomainValidationException("El ID proporcionado no corresponde a un estado de producto valido.");
            }

            productDomain.changeStatus(request.newStatusId());

            ProductDomain updatedProduct = productOutletRepositoryPort.save(productDomain);

            return productOutletMapper.toResponse(updatedProduct);
        });
    }
}
