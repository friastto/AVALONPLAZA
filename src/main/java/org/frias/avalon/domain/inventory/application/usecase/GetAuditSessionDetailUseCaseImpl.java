package org.frias.avalon.domain.inventory.application.usecase;

import org.frias.avalon.core.exeptions.ResourceNotFoundException;
import org.frias.avalon.core.tenant.TenantContext;
import org.frias.avalon.domain.inventory.application.dto.AuditSessionDetailResponse;
import org.frias.avalon.domain.inventory.infrastructure.entity.InventoryAuditItemEntity;
import org.frias.avalon.domain.inventory.infrastructure.entity.InventoryAuditSessionEntity;
import org.frias.avalon.domain.inventory.infrastructure.entity.InventoryAuditSignatureEntity;
import org.frias.avalon.domain.inventory.infrastructure.mapper.AuditMapper;
import org.frias.avalon.domain.inventory.infrastructure.repository.JpaInventoryAuditItemRepository;
import org.frias.avalon.domain.inventory.infrastructure.repository.JpaInventoryAuditSessionRepository;
import org.frias.avalon.domain.inventory.infrastructure.repository.JpaInventoryAuditSignatureRepository;
import org.frias.avalon.domain.outlet.domain.model.OutletDomain;
import org.frias.avalon.domain.outlet.domain.port.OutletRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;

@Service
public class GetAuditSessionDetailUseCaseImpl implements GetAuditSessionDetailUseCase {

    private final JpaInventoryAuditSessionRepository auditSessionRepository;
    private final JpaInventoryAuditItemRepository auditItemRepository;
    private final JpaInventoryAuditSignatureRepository auditSignatureRepository;
    private final AuditMapper auditMapper;
    private final OutletRepositoryPort outletRepositoryPort;
    private final TransactionTemplate transactionTemplate;

    public GetAuditSessionDetailUseCaseImpl(
            JpaInventoryAuditSessionRepository auditSessionRepository,
            JpaInventoryAuditItemRepository auditItemRepository,
            JpaInventoryAuditSignatureRepository auditSignatureRepository,
            AuditMapper auditMapper,
            OutletRepositoryPort outletRepositoryPort,
            PlatformTransactionManager transactionManager
    ) {
        this.auditSessionRepository = auditSessionRepository;
        this.auditItemRepository = auditItemRepository;
        this.auditSignatureRepository = auditSignatureRepository;
        this.auditMapper = auditMapper;
        this.outletRepositoryPort = outletRepositoryPort;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        this.transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    @Override
    public AuditSessionDetailResponse findById(Long auditSessionId) {
        InventoryAuditSessionEntity session = auditSessionRepository.findById(auditSessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Sesion de auditoria no encontrada con id: " + auditSessionId));

        Long previousCompanyId = TenantContext.getTenantId();
        Long previousOutletId = TenantContext.getTenantOutletId();

        if (session.getCompanyId() != null) {
            TenantContext.setTenantId(session.getCompanyId());
        }
        TenantContext.setTenantOutletId(session.getOutletId());

        try {
            return transactionTemplate.execute(status -> {
                List<InventoryAuditItemEntity> items = auditItemRepository.findByAuditSessionIdOrderByIdAsc(session.getId());
                List<InventoryAuditSignatureEntity> signatures = auditSignatureRepository.findByAuditSessionId(session.getId());
                return auditMapper.toDetailResponse(session, items, signatures);
            });
        } finally {
            TenantContext.setTenantId(previousCompanyId);
            TenantContext.setTenantOutletId(previousOutletId);
        }
    }

    @Override
    public AuditSessionDetailResponse findActiveByOutletId(Long outletId) {
        OutletDomain outlet = outletRepositoryPort.findById(outletId)
                .orElseThrow(() -> new ResourceNotFoundException("Outlet no encontrado con id: " + outletId));

        Long previousCompanyId = TenantContext.getTenantId();
        Long previousOutletId = TenantContext.getTenantOutletId();

        if (outlet.getCompanyId() != null) {
            TenantContext.setTenantId(outlet.getCompanyId());
        }
        TenantContext.setTenantOutletId(outletId);

        try {
            return transactionTemplate.execute(status -> {
                InventoryAuditSessionEntity session = auditSessionRepository
                        .findFirstByOutletIdAndStatus(outletId, "IN_PROGRESS")
                        .orElse(null);

                if (session == null) {
                    session = auditSessionRepository
                            .findFirstByOutletIdAndStatus(outletId, "REVIEWING")
                            .orElse(null);
                }

                if (session == null) {
                    return null;
                }

                List<InventoryAuditItemEntity> items = auditItemRepository.findByAuditSessionIdOrderByIdAsc(session.getId());
                List<InventoryAuditSignatureEntity> signatures = auditSignatureRepository.findByAuditSessionId(session.getId());
                return auditMapper.toDetailResponse(session, items, signatures);
            });
        } finally {
            TenantContext.setTenantId(previousCompanyId);
            TenantContext.setTenantOutletId(previousOutletId);
        }
    }
}
