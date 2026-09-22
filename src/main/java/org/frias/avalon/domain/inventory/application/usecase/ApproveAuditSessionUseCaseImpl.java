package org.frias.avalon.domain.inventory.application.usecase;

import org.frias.avalon.core.exeptions.BusinessException;
import org.frias.avalon.core.exeptions.ResourceNotFoundException;
import org.frias.avalon.core.tenant.TenantContext;
import org.frias.avalon.domain.inventory.application.dto.ApproveAuditSessionRequest;
import org.frias.avalon.domain.inventory.application.dto.AuditSessionDetailResponse;
import org.frias.avalon.domain.inventory.application.dto.AuditSignatureRequest;
import org.frias.avalon.domain.inventory.application.dto.AuditWebSocketMessage;
import org.frias.avalon.domain.inventory.application.dto.StockAdjustmentRequest;
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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ApproveAuditSessionUseCaseImpl implements ApproveAuditSessionUseCase {

    private final JpaInventoryAuditSessionRepository auditSessionRepository;
    private final JpaInventoryAuditItemRepository auditItemRepository;
    private final JpaInventoryAuditSignatureRepository auditSignatureRepository;
    private final StockAdjustmentUseCase stockAdjustmentUseCase;
    private final AuditMapper auditMapper;
    private final AuditWebSocketPublisher auditWebSocketPublisher;
    private final TransactionTemplate transactionTemplate;

    public ApproveAuditSessionUseCaseImpl(
            JpaInventoryAuditSessionRepository auditSessionRepository,
            JpaInventoryAuditItemRepository auditItemRepository,
            JpaInventoryAuditSignatureRepository auditSignatureRepository,
            StockAdjustmentUseCase stockAdjustmentUseCase,
            AuditMapper auditMapper,
            AuditWebSocketPublisher auditWebSocketPublisher,
            PlatformTransactionManager transactionManager
    ) {
        this.auditSessionRepository = auditSessionRepository;
        this.auditItemRepository = auditItemRepository;
        this.auditSignatureRepository = auditSignatureRepository;
        this.stockAdjustmentUseCase = stockAdjustmentUseCase;
        this.auditMapper = auditMapper;
        this.auditWebSocketPublisher = auditWebSocketPublisher;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        this.transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    @Override
    public AuditSessionDetailResponse execute(ApproveAuditSessionRequest request) {
        InventoryAuditSessionEntity session = auditSessionRepository.findById(request.auditSessionId())
                .orElseThrow(() -> new ResourceNotFoundException("Sesion de auditoria no encontrada con id: " + request.auditSessionId()));

        if ("COMPLETED".equalsIgnoreCase(session.getStatus())) {
            throw new BusinessException("Esta sesion de auditoria ya fue completada y aprobada previamente.");
        }

        Long previousCompanyId = TenantContext.getTenantId();
        Long previousOutletId = TenantContext.getTenantOutletId();

        if (session.getCompanyId() != null) {
            TenantContext.setTenantId(session.getCompanyId());
        }
        TenantContext.setTenantOutletId(session.getOutletId());

        try {
            return transactionTemplate.execute(status -> {
                // 1. Guardar firmas digitales
                List<InventoryAuditSignatureEntity> savedSignatures = new ArrayList<>();
                if (request.signatures() != null) {
                    for (AuditSignatureRequest sigReq : request.signatures()) {
                        InventoryAuditSignatureEntity signatureEntity = InventoryAuditSignatureEntity.builder()
                                .auditSession(session)
                                .signerType(sigReq.signerType())
                                .signerUserId(sigReq.signerUserId())
                                .signerName(sigReq.signerName())
                                .signatureBase64(sigReq.signatureBase64())
                                .signedAt(LocalDateTime.now())
                                .build();
                        savedSignatures.add(auditSignatureRepository.save(signatureEntity));
                    }
                }

                // 2. Procesar reajustes de stock en Kardex para items con diferencia
                List<InventoryAuditItemEntity> items = auditItemRepository.findByAuditSessionIdOrderByIdAsc(session.getId());
                for (InventoryAuditItemEntity item : items) {
                    if (item.getPhysicalStock() != null) {
                        if (item.getDifferenceStock() != null && item.getDifferenceStock().compareTo(BigDecimal.ZERO) != 0) {
                            String reason = item.getDiscrepancyReason() != null && !item.getDiscrepancyReason().isBlank()
                                    ? item.getDiscrepancyReason()
                                    : "Ajuste por Auditoria Fisica #" + session.getId();

                            stockAdjustmentUseCase.execute(new StockAdjustmentRequest(
                                    item.getProductOutletId(),
                                    session.getOutletId(),
                                    null,
                                    item.getPhysicalStock(),
                                    reason,
                                    request.approverUserId()
                            ));
                        }
                        item.setStatus("APPROVED");
                        item.setUpdatedAt(LocalDateTime.now());
                        auditItemRepository.save(item);
                    }
                }

                // 3. Finalizar la sesion
                session.setStatus("COMPLETED");
                session.setClosedByUserId(request.approverUserId());
                session.setClosedByName(request.approverName() != null ? request.approverName() : "Gerente #" + request.approverUserId());
                session.setClosedAt(LocalDateTime.now());
                if (request.notes() != null && !request.notes().isBlank()) {
                    session.setNotes(request.notes());
                }
                InventoryAuditSessionEntity finalSession = auditSessionRepository.save(session);

                AuditSessionDetailResponse response = auditMapper.toDetailResponse(finalSession, items, savedSignatures);

                auditWebSocketPublisher.broadcastAuditEvent(
                        session.getOutletId(),
                        session.getId(),
                        new AuditWebSocketMessage("SESSION_APPROVED", session.getId(), session.getOutletId(), response, null)
                );

                return response;
            });
        } finally {
            TenantContext.setTenantId(previousCompanyId);
            TenantContext.setTenantOutletId(previousOutletId);
        }
    }
}
