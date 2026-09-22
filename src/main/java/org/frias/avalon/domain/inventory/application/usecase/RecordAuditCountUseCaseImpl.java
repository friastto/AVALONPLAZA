package org.frias.avalon.domain.inventory.application.usecase;

import org.frias.avalon.core.exeptions.BusinessException;
import org.frias.avalon.core.exeptions.ResourceNotFoundException;
import org.frias.avalon.core.tenant.TenantContext;
import org.frias.avalon.domain.inventory.application.dto.AuditItemCountRequest;
import org.frias.avalon.domain.inventory.application.dto.AuditItemDto;
import org.frias.avalon.domain.inventory.application.dto.AuditSessionDetailResponse;
import org.frias.avalon.domain.inventory.application.dto.AuditWebSocketMessage;
import org.frias.avalon.domain.inventory.infrastructure.entity.InventoryAuditItemEntity;
import org.frias.avalon.domain.inventory.infrastructure.entity.InventoryAuditSessionEntity;
import org.frias.avalon.domain.inventory.infrastructure.entity.InventoryAuditSignatureEntity;
import org.frias.avalon.domain.inventory.infrastructure.mapper.AuditMapper;
import org.frias.avalon.domain.inventory.infrastructure.repository.JpaInventoryAuditItemRepository;
import org.frias.avalon.domain.inventory.infrastructure.repository.JpaInventoryAuditSessionRepository;
import org.frias.avalon.domain.inventory.infrastructure.repository.JpaInventoryAuditSignatureRepository;
import org.frias.avalon.domain.inventory.presentation.AuditWebSocketPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class RecordAuditCountUseCaseImpl implements RecordAuditCountUseCase {

    private final JpaInventoryAuditSessionRepository auditSessionRepository;
    private final JpaInventoryAuditItemRepository auditItemRepository;
    private final JpaInventoryAuditSignatureRepository auditSignatureRepository;
    private final AuditMapper auditMapper;
    private final AuditWebSocketPublisher auditWebSocketPublisher;
    private final TransactionTemplate transactionTemplate;

    public RecordAuditCountUseCaseImpl(
            JpaInventoryAuditSessionRepository auditSessionRepository,
            JpaInventoryAuditItemRepository auditItemRepository,
            JpaInventoryAuditSignatureRepository auditSignatureRepository,
            AuditMapper auditMapper,
            AuditWebSocketPublisher auditWebSocketPublisher,
            PlatformTransactionManager transactionManager
    ) {
        this.auditSessionRepository = auditSessionRepository;
        this.auditItemRepository = auditItemRepository;
        this.auditSignatureRepository = auditSignatureRepository;
        this.auditMapper = auditMapper;
        this.auditWebSocketPublisher = auditWebSocketPublisher;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        this.transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    @Override
    public AuditItemDto execute(AuditItemCountRequest request) {
        InventoryAuditSessionEntity session = auditSessionRepository.findById(request.auditSessionId())
                .orElseThrow(() -> new ResourceNotFoundException("Sesion de auditoria no encontrada con id: " + request.auditSessionId()));

        if (!"IN_PROGRESS".equalsIgnoreCase(session.getStatus()) && !"REVIEWING".equalsIgnoreCase(session.getStatus())) {
            throw new BusinessException("No se pueden registrar conteos en una sesion con estado: " + session.getStatus());
        }

        Long previousCompanyId = TenantContext.getTenantId();
        Long previousOutletId = TenantContext.getTenantOutletId();

        if (session.getCompanyId() != null) {
            TenantContext.setTenantId(session.getCompanyId());
        }
        TenantContext.setTenantOutletId(session.getOutletId());

        try {
            return transactionTemplate.execute(status -> {
                InventoryAuditItemEntity item = auditItemRepository
                        .findByAuditSessionIdAndProductOutletId(request.auditSessionId(), request.productOutletId())
                        .orElseThrow(() -> new ResourceNotFoundException("Item de auditoria no encontrado para producto: " + request.productOutletId()));

                BigDecimal physicalStock = request.physicalStock();
                BigDecimal physicalValue = item.getUnitPrice().multiply(physicalStock).setScale(2, RoundingMode.HALF_UP);
                BigDecimal differenceStock = physicalStock.subtract(item.getSoftwareStock());
                BigDecimal differenceValue = physicalValue.subtract(item.getSoftwareValue());

                item.setPhysicalStock(physicalStock);
                item.setPhysicalValue(physicalValue);
                item.setDifferenceStock(differenceStock);
                item.setDifferenceValue(differenceValue);
                item.setDiscrepancyReason(request.discrepancyReason());
                item.setCountedByUserId(request.operatorId());
                item.setCountedByName(request.operatorName() != null ? request.operatorName() : "Operador #" + request.operatorId());
                item.setStatus("COUNTED");
                item.setUpdatedAt(LocalDateTime.now());

                InventoryAuditItemEntity savedItem = auditItemRepository.save(item);

                // Recalcular totales de la sesion
                List<InventoryAuditItemEntity> allItems = auditItemRepository.findByAuditSessionIdOrderByIdAsc(session.getId());
                BigDecimal totalPhysicalValue = BigDecimal.ZERO;
                for (InventoryAuditItemEntity it : allItems) {
                    if (it.getPhysicalValue() != null) {
                        totalPhysicalValue = totalPhysicalValue.add(it.getPhysicalValue());
                    }
                }
                BigDecimal totalDifferenceValue = totalPhysicalValue.subtract(
                        session.getTotalSoftwareValue() != null ? session.getTotalSoftwareValue() : BigDecimal.ZERO
                );

                session.setTotalPhysicalValue(totalPhysicalValue);
                session.setTotalDifferenceValue(totalDifferenceValue);
                auditSessionRepository.save(session);

                List<InventoryAuditSignatureEntity> signatures = auditSignatureRepository.findByAuditSessionId(session.getId());
                AuditSessionDetailResponse sessionResponse = auditMapper.toDetailResponse(session, allItems, signatures);
                AuditItemDto itemDto = auditMapper.toItemDto(savedItem);

                auditWebSocketPublisher.broadcastAuditEvent(
                        session.getOutletId(),
                        session.getId(),
                        new AuditWebSocketMessage("ITEM_COUNTED", session.getId(), session.getOutletId(), sessionResponse, itemDto)
                );

                return itemDto;
            });
        } finally {
            TenantContext.setTenantId(previousCompanyId);
            TenantContext.setTenantOutletId(previousOutletId);
        }
    }
}
