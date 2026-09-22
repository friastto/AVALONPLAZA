package org.frias.avalon.domain.inventory.application.usecase;

import org.frias.avalon.core.exeptions.ResourceNotFoundException;
import org.frias.avalon.core.tenant.TenantContext;
import org.frias.avalon.domain.inventory.application.dto.AuditSessionDetailResponse;
import org.frias.avalon.domain.inventory.application.dto.AuditWebSocketMessage;
import org.frias.avalon.domain.inventory.application.dto.CreateAuditSessionRequest;
import org.frias.avalon.domain.inventory.infrastructure.entity.InventoryAuditItemEntity;
import org.frias.avalon.domain.inventory.infrastructure.entity.InventoryAuditSessionEntity;
import org.frias.avalon.domain.inventory.infrastructure.mapper.AuditMapper;
import org.frias.avalon.domain.inventory.infrastructure.repository.JpaInventoryAuditItemRepository;
import org.frias.avalon.domain.inventory.infrastructure.repository.JpaInventoryAuditSessionRepository;
import org.frias.avalon.domain.inventory.infrastructure.repository.JpaInventoryAuditSignatureRepository;
import org.frias.avalon.domain.inventory.presentation.AuditWebSocketPublisher;
import org.frias.avalon.domain.masterdata.domain.model.MasterRoot;
import org.frias.avalon.domain.masterdata.domain.model.MasterTree;
import org.frias.avalon.domain.masterdata.domain.service.MasterTreeProvider;
import org.frias.avalon.domain.outlet.domain.model.OutletDomain;
import org.frias.avalon.domain.outlet.domain.port.OutletRepositoryPort;
import org.frias.avalon.domain.product.infraestructure.entity.ProductOutlet;
import org.frias.avalon.domain.product.infraestructure.repository.JpaProductOutletRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class CreateAuditSessionUseCaseImpl implements CreateAuditSessionUseCase {

    private final JpaInventoryAuditSessionRepository auditSessionRepository;
    private final JpaInventoryAuditItemRepository auditItemRepository;
    private final JpaInventoryAuditSignatureRepository auditSignatureRepository;
    private final JpaProductOutletRepository productOutletRepository;
    private final MasterTreeProvider masterTreeProvider;
    private final AuditMapper auditMapper;
    private final AuditWebSocketPublisher auditWebSocketPublisher;
    private final OutletRepositoryPort outletRepositoryPort;
    private final TransactionTemplate transactionTemplate;

    public CreateAuditSessionUseCaseImpl(
            JpaInventoryAuditSessionRepository auditSessionRepository,
            JpaInventoryAuditItemRepository auditItemRepository,
            JpaInventoryAuditSignatureRepository auditSignatureRepository,
            JpaProductOutletRepository productOutletRepository,
            MasterTreeProvider masterTreeProvider,
            AuditMapper auditMapper,
            AuditWebSocketPublisher auditWebSocketPublisher,
            OutletRepositoryPort outletRepositoryPort,
            PlatformTransactionManager transactionManager
    ) {
        this.auditSessionRepository = auditSessionRepository;
        this.auditItemRepository = auditItemRepository;
        this.auditSignatureRepository = auditSignatureRepository;
        this.productOutletRepository = productOutletRepository;
        this.masterTreeProvider = masterTreeProvider;
        this.auditMapper = auditMapper;
        this.auditWebSocketPublisher = auditWebSocketPublisher;
        this.outletRepositoryPort = outletRepositoryPort;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        this.transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    @Override
    public AuditSessionDetailResponse execute(CreateAuditSessionRequest request) {
        Long outletId = request.outletId();
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
                // Si ya existe una sesion activa en progreso, reutilizarla
                Optional<InventoryAuditSessionEntity> activeOpt = auditSessionRepository
                        .findFirstByOutletIdAndStatus(outletId, "IN_PROGRESS");

                if (activeOpt.isPresent()) {
                    InventoryAuditSessionEntity existing = activeOpt.get();
                    List<InventoryAuditItemEntity> items = auditItemRepository
                            .findByAuditSessionIdOrderByIdAsc(existing.getId());
                    return auditMapper.toDetailResponse(existing, items, Collections.emptyList());
                }

                InventoryAuditSessionEntity session = InventoryAuditSessionEntity.builder()
                        .outletId(outletId)
                        .companyId(outlet.getCompanyId())
                        .status("IN_PROGRESS")
                        .openedByUserId(request.operatorId())
                        .openedByName(request.operatorName() != null ? request.operatorName() : "Operador #" + request.operatorId())
                        .notes(request.notes())
                        .totalSoftwareValue(BigDecimal.ZERO)
                        .totalPhysicalValue(BigDecimal.ZERO)
                        .totalDifferenceValue(BigDecimal.ZERO)
                        .openedAt(LocalDateTime.now())
                        .build();

                InventoryAuditSessionEntity savedSession = auditSessionRepository.save(session);

                List<ProductOutlet> outletProducts = productOutletRepository.findAll();
                List<InventoryAuditItemEntity> itemsToSave = new ArrayList<>();
                BigDecimal totalSoftwareValue = BigDecimal.ZERO;

                MasterTree tree = masterTreeProvider != null ? masterTreeProvider.getTree() : null;

                for (ProductOutlet po : outletProducts) {
                    String unitCode = "UND";
                    if (tree != null && po.getUnitMeasureId() != null) {
                        MasterRoot unitNode = tree.getById(po.getUnitMeasureId());
                        if (unitNode != null && unitNode.getShortName() != null) {
                            unitCode = unitNode.getShortName().toUpperCase();
                        }
                    }

                    int baseStock = po.getStock() != null ? po.getStock() : 0;
                    BigDecimal softwareStock;
                    if ("KG".equals(unitCode) || "L".equals(unitCode) || "LT".equals(unitCode)) {
                        softwareStock = BigDecimal.valueOf(baseStock).divide(BigDecimal.valueOf(1000), 3, RoundingMode.HALF_UP);
                    } else if ("LB".equals(unitCode)) {
                        softwareStock = BigDecimal.valueOf(baseStock).divide(BigDecimal.valueOf(453.592), 3, RoundingMode.HALF_UP);
                    } else {
                        softwareStock = BigDecimal.valueOf(baseStock);
                    }

                    BigDecimal unitPrice = po.getLocalPrice() != null ? po.getLocalPrice() : BigDecimal.ZERO;
                    BigDecimal softwareValue = unitPrice.multiply(softwareStock).setScale(2, RoundingMode.HALF_UP);
                    totalSoftwareValue = totalSoftwareValue.add(softwareValue);

                    String name = po.getLocalName() != null ? po.getLocalName() : "Producto #" + po.getId();
                    String barcode = null;
                    if (po.getProductCompany() != null) {
                        barcode = po.getProductCompany().getProductId() != null ? String.valueOf(po.getProductCompany().getProductId()) : null;
                    }

                    InventoryAuditItemEntity item = InventoryAuditItemEntity.builder()
                            .auditSession(savedSession)
                            .productOutletId(po.getId())
                            .productName(name)
                            .barcode(barcode)
                            .unitMeasure(unitCode)
                            .unitPrice(unitPrice)
                            .softwareStock(softwareStock)
                            .softwareValue(softwareValue)
                            .physicalStock(null)
                            .physicalValue(null)
                            .differenceStock(null)
                            .differenceValue(null)
                            .status("PENDING")
                            .updatedAt(LocalDateTime.now())
                            .build();

                    itemsToSave.add(item);
                }

                savedSession.setTotalSoftwareValue(totalSoftwareValue);
                auditSessionRepository.save(savedSession);
                List<InventoryAuditItemEntity> savedItems = auditItemRepository.saveAll(itemsToSave);

                AuditSessionDetailResponse response = auditMapper.toDetailResponse(savedSession, savedItems, Collections.emptyList());

                auditWebSocketPublisher.broadcastAuditEvent(
                        outletId,
                        savedSession.getId(),
                        new AuditWebSocketMessage("SESSION_CREATED", savedSession.getId(), outletId, response, null)
                );

                return response;
            });
        } finally {
            TenantContext.setTenantId(previousCompanyId);
            TenantContext.setTenantOutletId(previousOutletId);
        }
    }
}
