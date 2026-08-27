package org.frias.avalon.domain.sale.infrastructure.adapter;

import org.frias.avalon.domain.sale.domain.ReturnDomain;
import org.frias.avalon.domain.sale.domain.ReturnItemDomain;
import org.frias.avalon.domain.sale.infrastructure.entity.ReturnEntity;
import org.frias.avalon.domain.sale.infrastructure.entity.ReturnItemEntity;
import org.frias.avalon.domain.sale.infrastructure.mapper.ReturnMapper;
import org.frias.avalon.domain.sale.infrastructure.repository.JpaReturnRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para ReturnRepositoryAdapter.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Unit Tests for ReturnRepositoryAdapter")
class ReturnRepositoryAdapterTest {

    @Mock
    private JpaReturnRepository jpaReturnRepository;

    @Mock
    private ReturnMapper returnMapper;

    @InjectMocks
    private ReturnRepositoryAdapter returnRepositoryAdapter;

    private ReturnEntity sampleEntity;
    private ReturnDomain sampleDomain;
    private UUID sampleUuid;
    private LocalDateTime sampleTime;

    @BeforeEach
    void setUp() {
        sampleUuid = UUID.randomUUID();
        sampleTime = LocalDateTime.now();

        ReturnItemDomain itemDomain = new ReturnItemDomain(
                1L, 100L, 2, "2 UND",
                new BigDecimal("50.00"), new BigDecimal("100.00"), 5L
        );

        sampleDomain = ReturnDomain.fromPersistence(
                10L, sampleUuid, 1000L, new BigDecimal("100.00"),
                "DEFECTO", "Producto dañado", "REEMBOLSO",
                1L, 2L, 3L, 4L,
                sampleTime, sampleTime, sampleTime, List.of(itemDomain)
        );

        ReturnItemEntity itemEntity = ReturnItemEntity.builder()
                .id(1L)
                .productId(100L)
                .quantityInBaseUnits(2)
                .displayQuantity("2 UND")
                .unitPrice(new BigDecimal("50.00"))
                .subtotal(new BigDecimal("100.00"))
                .unitMeasureId(5L)
                .build();

        sampleEntity = ReturnEntity.builder()
                .id(10L)
                .returnCode(sampleUuid)
                .originalSaleId(1000L)
                .totalRefundAmount(new BigDecimal("100.00"))
                .reason("DEFECTO")
                .notes("Producto dañado")
                .resolutionType("REEMBOLSO")
                .statusId(1L)
                .employeeId(2L)
                .outletId(3L)
                .clientId(4L)
                .returnDate(sampleTime)
                .createdAt(sampleTime)
                .updatedAt(sampleTime)
                .items(List.of(itemEntity))
                .build();
    }

    @Test
    @DisplayName("save should map to entity, invoke repository save, and map back to domain")
    void save_WithValidDomain_MapsSavesAndReturnsDomain() {
        ReturnEntity mappedEntity = ReturnEntity.builder().returnCode(sampleUuid).build();
        ReturnEntity savedEntity = sampleEntity;

        when(returnMapper.toEntity(sampleDomain)).thenReturn(mappedEntity);
        when(jpaReturnRepository.save(mappedEntity)).thenReturn(savedEntity);
        when(returnMapper.toDomain(savedEntity)).thenReturn(sampleDomain);

        ReturnDomain result = returnRepositoryAdapter.save(sampleDomain);

        assertNotNull(result);
        assertEquals(sampleDomain.getId(), result.getId());
        assertEquals(sampleDomain.getReturnCode(), result.getReturnCode());

        verify(returnMapper, times(1)).toEntity(sampleDomain);
        verify(jpaReturnRepository, times(1)).save(mappedEntity);
        verify(returnMapper, times(1)).toDomain(savedEntity);
    }

    @Test
    @DisplayName("findByCode should return mapped domain when return is found")
    void findByCode_WhenExists_ReturnsMappedDomain() {
        when(jpaReturnRepository.findByReturnCode(sampleUuid)).thenReturn(Optional.of(sampleEntity));
        when(returnMapper.toDomain(sampleEntity)).thenReturn(sampleDomain);

        Optional<ReturnDomain> result = returnRepositoryAdapter.findByCode(sampleUuid);

        assertTrue(result.isPresent());
        assertEquals(sampleDomain.getId(), result.get().getId());
        assertEquals(sampleUuid, result.get().getReturnCode());
        verify(jpaReturnRepository, times(1)).findByReturnCode(sampleUuid);
        verify(returnMapper, times(1)).toDomain(sampleEntity);
    }

    @Test
    @DisplayName("findByCode should return empty optional when return code is not found")
    void findByCode_WhenNotFound_ReturnsEmptyOptional() {
        UUID nonExistentCode = UUID.randomUUID();
        when(jpaReturnRepository.findByReturnCode(nonExistentCode)).thenReturn(Optional.empty());

        Optional<ReturnDomain> result = returnRepositoryAdapter.findByCode(nonExistentCode);

        assertTrue(result.isEmpty());
        verify(jpaReturnRepository, times(1)).findByReturnCode(nonExistentCode);
        verify(returnMapper, never()).toDomain(any());
    }

    @Test
    @DisplayName("findByOriginalSaleId should return mapped domain list when returns exist")
    void findByOriginalSaleId_WhenReturnsExist_ReturnsMappedDomainList() {
        Long originalSaleId = 1000L;
        when(jpaReturnRepository.findByOriginalSaleId(originalSaleId)).thenReturn(List.of(sampleEntity));
        when(returnMapper.toDomain(sampleEntity)).thenReturn(sampleDomain);

        List<ReturnDomain> result = returnRepositoryAdapter.findByOriginalSaleId(originalSaleId);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(sampleDomain.getId(), result.get(0).getId());
        verify(jpaReturnRepository, times(1)).findByOriginalSaleId(originalSaleId);
        verify(returnMapper, times(1)).toDomain(sampleEntity);
    }

    @Test
    @DisplayName("findByOriginalSaleId should return empty list when no returns exist for sale")
    void findByOriginalSaleId_WhenNoReturnsExist_ReturnsEmptyList() {
        Long originalSaleId = 9999L;
        when(jpaReturnRepository.findByOriginalSaleId(originalSaleId)).thenReturn(List.of());

        List<ReturnDomain> result = returnRepositoryAdapter.findByOriginalSaleId(originalSaleId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(jpaReturnRepository, times(1)).findByOriginalSaleId(originalSaleId);
        verify(returnMapper, never()).toDomain(any());
    }

    @Test
    @DisplayName("findByOutletId should return mapped page of domain returns")
    void findByOutletId_WhenReturnsExist_ReturnsMappedPage() {
        Long outletId = 3L;
        Pageable pageable = PageRequest.of(0, 10);
        Page<ReturnEntity> entityPage = new PageImpl<>(List.of(sampleEntity), pageable, 1);

        when(jpaReturnRepository.findByOutletId(outletId, pageable)).thenReturn(entityPage);
        when(returnMapper.toDomain(sampleEntity)).thenReturn(sampleDomain);

        Page<ReturnDomain> result = returnRepositoryAdapter.findByOutletId(outletId, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(sampleDomain.getId(), result.getContent().get(0).getId());

        verify(jpaReturnRepository, times(1)).findByOutletId(outletId, pageable);
        verify(returnMapper, times(1)).toDomain(sampleEntity);
    }

    @Test
    @DisplayName("findByOutletId should return empty page when no returns exist")
    void findByOutletId_WhenNoReturns_ReturnsEmptyPage() {
        Long outletId = 3L;
        Pageable pageable = PageRequest.of(0, 10);
        Page<ReturnEntity> emptyPage = new PageImpl<>(List.of(), pageable, 0);

        when(jpaReturnRepository.findByOutletId(outletId, pageable)).thenReturn(emptyPage);

        Page<ReturnDomain> result = returnRepositoryAdapter.findByOutletId(outletId, pageable);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        assertEquals(0, result.getTotalElements());
        verify(returnMapper, never()).toDomain(any());
    }
}
