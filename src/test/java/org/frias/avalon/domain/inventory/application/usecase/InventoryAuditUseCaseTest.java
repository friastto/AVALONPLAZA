package org.frias.avalon.domain.inventory.application.usecase;

import org.frias.avalon.domain.inventory.application.dto.AuditItemCountRequest;
import org.frias.avalon.domain.inventory.application.dto.AuditItemDto;
import org.frias.avalon.domain.inventory.infrastructure.entity.InventoryAuditItemEntity;
import org.frias.avalon.domain.inventory.infrastructure.entity.InventoryAuditSessionEntity;
import org.frias.avalon.domain.inventory.infrastructure.mapper.AuditMapper;
import org.frias.avalon.domain.inventory.infrastructure.repository.JpaInventoryAuditItemRepository;
import org.frias.avalon.domain.inventory.infrastructure.repository.JpaInventoryAuditSessionRepository;
import org.frias.avalon.domain.inventory.infrastructure.repository.JpaInventoryAuditSignatureRepository;
import org.frias.avalon.domain.inventory.presentation.AuditWebSocketPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventoryAuditUseCaseTest {

    @Mock
    private JpaInventoryAuditSessionRepository auditSessionRepository;

    @Mock
    private JpaInventoryAuditItemRepository auditItemRepository;

    @Mock
    private JpaInventoryAuditSignatureRepository auditSignatureRepository;

    @Mock
    private AuditWebSocketPublisher auditWebSocketPublisher;

    @Mock
    private PlatformTransactionManager transactionManager;

    private AuditMapper auditMapper;
    private RecordAuditCountUseCaseImpl recordAuditCountUseCase;

    @BeforeEach
    void setUp() {
        auditMapper = new AuditMapper();
        TransactionStatus dummyStatus = mock(TransactionStatus.class);
        doAnswer(invocation -> null).when(transactionManager).commit(any());

        recordAuditCountUseCase = new RecordAuditCountUseCaseImpl(
                auditSessionRepository,
                auditItemRepository,
                auditSignatureRepository,
                auditMapper,
                auditWebSocketPublisher,
                transactionManager
        );
    }

    @Test
    void testRecordCountCalculatesMonetaryImpactCorrectly() {
        Long sessionId = 1L;
        Long productOutletId = 10L;

        InventoryAuditSessionEntity session = InventoryAuditSessionEntity.builder()
                .id(sessionId)
                .outletId(100L)
                .companyId(10L)
                .status("IN_PROGRESS")
                .totalSoftwareValue(BigDecimal.valueOf(50000))
                .totalPhysicalValue(BigDecimal.ZERO)
                .totalDifferenceValue(BigDecimal.ZERO)
                .openedAt(LocalDateTime.now())
                .build();

        InventoryAuditItemEntity item = InventoryAuditItemEntity.builder()
                .id(1L)
                .auditSession(session)
                .productOutletId(productOutletId)
                .productName("Arroz Diana 500g")
                .unitMeasure("UND")
                .unitPrice(BigDecimal.valueOf(2500))
                .softwareStock(BigDecimal.valueOf(20))
                .softwareValue(BigDecimal.valueOf(50000))
                .status("PENDING")
                .updatedAt(LocalDateTime.now())
                .build();

        when(auditSessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
        when(auditItemRepository.findByAuditSessionIdAndProductOutletId(sessionId, productOutletId)).thenReturn(Optional.of(item));
        when(auditItemRepository.save(any(InventoryAuditItemEntity.class))).thenAnswer(i -> i.getArgument(0));
        when(auditItemRepository.findByAuditSessionIdOrderByIdAsc(sessionId)).thenReturn(List.of(item));
        when(auditSessionRepository.save(any(InventoryAuditSessionEntity.class))).thenAnswer(i -> i.getArgument(0));
        when(auditSignatureRepository.findByAuditSessionId(sessionId)).thenReturn(Collections.emptyList());

        AuditItemCountRequest request = new AuditItemCountRequest(
                sessionId,
                productOutletId,
                BigDecimal.valueOf(18),
                "Averia empaque",
                5L,
                "Pedro Perez"
        );

        AuditItemDto result = recordAuditCountUseCase.execute(request);

        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(18), result.physicalStock());
        assertEquals(new BigDecimal("45000.00"), result.physicalValue());
        assertEquals(new BigDecimal("-2"), result.differenceStock());
        assertEquals(new BigDecimal("-5000.00"), result.differenceValue());
        assertEquals("COUNTED", result.status());
        assertEquals("Pedro Perez", result.countedByName());
    }
}
