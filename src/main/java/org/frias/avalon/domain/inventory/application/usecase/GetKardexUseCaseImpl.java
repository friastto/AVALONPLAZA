package org.frias.avalon.domain.inventory.application.usecase;

import org.frias.avalon.core.tenant.TenantContext;
import org.frias.avalon.domain.inventory.application.dto.KardexResponseDto;
import org.frias.avalon.domain.inventory.infrastructure.entity.StockMovementEntity;
import org.frias.avalon.domain.inventory.infrastructure.repository.JpaStockMovementRepository;
import org.frias.avalon.domain.outlet.infraestructure.entities.Outlet;
import org.frias.avalon.domain.outlet.infraestructure.repository.JpaOutletRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Collections;
import java.util.List;

/**
 * Implementation of GetKardexUseCase Input Port.
 * Supports multi-tenant schema switching and transactional isolation.
 */
@Service
public class GetKardexUseCaseImpl implements GetKardexUseCase {

    private final JpaStockMovementRepository stockMovementRepository;
    private final JpaOutletRepository jpaOutletRepository;
    private final TransactionTemplate transactionTemplate;

    public GetKardexUseCaseImpl(
            JpaStockMovementRepository stockMovementRepository,
            JpaOutletRepository jpaOutletRepository,
            PlatformTransactionManager transactionManager) {
        this.stockMovementRepository = stockMovementRepository;
        this.jpaOutletRepository = jpaOutletRepository;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        this.transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    @Override
    public List<KardexResponseDto> findByProductOutletId(Long productOutletId) {
        Long previousOutletId = TenantContext.getTenantOutletId();
        Long previousTenantId = TenantContext.getTenantId();

        try {
            if (previousOutletId != null) {
                List<KardexResponseDto> result = getKardexForProductInCurrentTenant(productOutletId);
                if (result != null && !result.isEmpty()) {
                    return result;
                }
            }

            List<Outlet> outlets = jpaOutletRepository.findAll();
            for (Outlet outlet : outlets) {
                if (outlet.getCompanyId() != null) {
                    TenantContext.setTenantId(outlet.getCompanyId());
                }
                TenantContext.setTenantOutletId(outlet.getId());

                List<KardexResponseDto> result = getKardexForProductInCurrentTenant(productOutletId);
                if (result != null && !result.isEmpty()) {
                    return result;
                }
            }

            return Collections.emptyList();
        } finally {
            TenantContext.setTenantId(previousTenantId);
            TenantContext.setTenantOutletId(previousOutletId);
        }
    }

    private List<KardexResponseDto> getKardexForProductInCurrentTenant(Long productOutletId) {
        try {
            return transactionTemplate.execute(status ->
                    stockMovementRepository.findByProductOutletIdOrderByCreatedAtDesc(productOutletId).stream()
                            .map(this::toDto)
                            .toList()
            );
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    @Override
    public List<KardexResponseDto> findByOutletId(Long outletId) {
        Long previousOutletId = TenantContext.getTenantOutletId();
        Long previousTenantId = TenantContext.getTenantId();

        try {
            if (outletId != null) {
                Outlet outlet = jpaOutletRepository.findById(outletId).orElse(null);
                if (outlet != null) {
                    if (outlet.getCompanyId() != null) {
                        TenantContext.setTenantId(outlet.getCompanyId());
                    }
                    TenantContext.setTenantOutletId(outlet.getId());
                }
            }

            return transactionTemplate.execute(status ->
                    stockMovementRepository.findByOutletIdOrderByCreatedAtDesc(outletId).stream()
                            .map(this::toDto)
                            .toList()
            );
        } finally {
            TenantContext.setTenantId(previousTenantId);
            TenantContext.setTenantOutletId(previousOutletId);
        }
    }

    private KardexResponseDto toDto(StockMovementEntity entity) {
        return new KardexResponseDto(
                entity.getId(),
                entity.getProductOutletId(),
                entity.getOutletId(),
                entity.getMovementType(),
                entity.getQuantityBefore(),
                entity.getQuantityAfter(),
                entity.getQuantityDelta(),
                entity.getReason(),
                entity.getOperatorId(),
                entity.getCreatedAt()
        );
    }
}
