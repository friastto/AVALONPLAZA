package org.frias.avalon.domain.sale.infrastructure.adapter;

import org.frias.avalon.domain.sale.domain.SaleDomain;
import org.frias.avalon.domain.sale.domain.SaleItemDomain;
import org.frias.avalon.domain.sale.infrastructure.entity.SaleEntity;
import org.frias.avalon.domain.sale.infrastructure.entity.SaleItemEntity;
import org.frias.avalon.domain.sale.infrastructure.mapper.SaleMapper;
import org.frias.avalon.domain.sale.infrastructure.repository.JpaSaleRepository;
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

@ExtendWith(MockitoExtension.class)
@DisplayName("Unit Tests for SaleRepositoryAdapter")
class SaleRepositoryAdapterTest {

    @Mock
    private JpaSaleRepository jpaSaleRepository;

    @Mock
    private SaleMapper saleMapper;

    @InjectMocks
    private SaleRepositoryAdapter saleRepositoryAdapter;

    private SaleEntity sampleEntity;
    private SaleDomain sampleDomain;
    private UUID sampleUuid;
    private LocalDateTime sampleTime;

    @BeforeEach
    void setUp() {
        sampleUuid = UUID.randomUUID();
        sampleTime = LocalDateTime.now();

        SaleItemDomain itemDomain = new SaleItemDomain(
                1L, 100L, 2, "2 UND",
                new BigDecimal("50.00"), new BigDecimal("100.00"), 5L
        );

        sampleDomain = SaleDomain.fromPersistence(
                10L, sampleUuid, new BigDecimal("100.00"), new BigDecimal("100.00"),
                BigDecimal.ZERO, 1L, 2L, 3L, 4L, 5L,
                sampleTime, sampleTime, sampleTime, List.of(itemDomain)
        );

        SaleItemEntity itemEntity = SaleItemEntity.builder()
                .id(1L)
                .productId(100L)
                .quantityInBaseUnits(2)
                .displayQuantity("2 UND")
                .unitPrice(new BigDecimal("50.00"))
                .subtotal(new BigDecimal("100.00"))
                .unitMeasureId(5L)
                .build();

        sampleEntity = SaleEntity.builder()
                .id(10L)
                .saleCode(sampleUuid)
                .totalAmount(new BigDecimal("100.00"))
                .amountReceived(new BigDecimal("100.00"))
                .changeGiven(BigDecimal.ZERO)
                .paymentMethodId(1L)
                .statusId(2L)
                .clientId(3L)
                .outletId(4L)
                .employeeId(5L)
                .saleDate(sampleTime)
                .createdAt(sampleTime)
                .updatedAt(sampleTime)
                .items(List.of(itemEntity))
                .build();
    }

    @Test
    @DisplayName("save should map to entity, invoke repository save, and map back to domain")
    void save_WithValidDomain_MapsSavesAndReturnsDomain() {
        SaleEntity mappedEntity = SaleEntity.builder().saleCode(sampleUuid).build();
        SaleEntity savedEntity = sampleEntity;

        when(saleMapper.toEntity(sampleDomain)).thenReturn(mappedEntity);
        when(jpaSaleRepository.save(mappedEntity)).thenReturn(savedEntity);
        when(saleMapper.toDomain(savedEntity)).thenReturn(sampleDomain);

        SaleDomain result = saleRepositoryAdapter.save(sampleDomain);

        assertNotNull(result);
        assertEquals(sampleDomain.getId(), result.getId());
        assertEquals(sampleDomain.getSaleCode(), result.getSaleCode());

        verify(saleMapper, times(1)).toEntity(sampleDomain);
        verify(jpaSaleRepository, times(1)).save(mappedEntity);
        verify(saleMapper, times(1)).toDomain(savedEntity);
    }

    @Test
    @DisplayName("findByCode should return mapped domain when sale is found")
    void findByCode_WhenExists_ReturnsMappedDomain() {
        when(jpaSaleRepository.findBySaleCode(sampleUuid)).thenReturn(Optional.of(sampleEntity));
        when(saleMapper.toDomain(sampleEntity)).thenReturn(sampleDomain);

        Optional<SaleDomain> result = saleRepositoryAdapter.findByCode(sampleUuid);

        assertTrue(result.isPresent());
        assertEquals(sampleDomain.getId(), result.get().getId());
        assertEquals(sampleUuid, result.get().getSaleCode());
        verify(jpaSaleRepository, times(1)).findBySaleCode(sampleUuid);
        verify(saleMapper, times(1)).toDomain(sampleEntity);
    }

    @Test
    @DisplayName("findByCode should return empty optional when sale code is not found")
    void findByCode_WhenNotFound_ReturnsEmptyOptional() {
        UUID nonExistentCode = UUID.randomUUID();
        when(jpaSaleRepository.findBySaleCode(nonExistentCode)).thenReturn(Optional.empty());

        Optional<SaleDomain> result = saleRepositoryAdapter.findByCode(nonExistentCode);

        assertTrue(result.isEmpty());
        verify(jpaSaleRepository, times(1)).findBySaleCode(nonExistentCode);
        verify(saleMapper, never()).toDomain(any());
    }

    @Test
    @DisplayName("findById should return mapped domain when ID exists")
    void findById_WhenExists_ReturnsMappedDomain() {
        Long id = 10L;
        when(jpaSaleRepository.findById(id)).thenReturn(Optional.of(sampleEntity));
        when(saleMapper.toDomain(sampleEntity)).thenReturn(sampleDomain);

        Optional<SaleDomain> result = saleRepositoryAdapter.findById(id);

        assertTrue(result.isPresent());
        assertEquals(id, result.get().getId());
        verify(jpaSaleRepository, times(1)).findById(id);
        verify(saleMapper, times(1)).toDomain(sampleEntity);
    }

    @Test
    @DisplayName("findById should return empty optional when ID does not exist")
    void findById_WhenNotFound_ReturnsEmptyOptional() {
        Long id = 999L;
        when(jpaSaleRepository.findById(id)).thenReturn(Optional.empty());

        Optional<SaleDomain> result = saleRepositoryAdapter.findById(id);

        assertTrue(result.isEmpty());
        verify(jpaSaleRepository, times(1)).findById(id);
        verify(saleMapper, never()).toDomain(any());
    }

    @Test
    @DisplayName("findByOutletId should return mapped page of domain sales")
    void findByOutletId_WhenSalesExist_ReturnsMappedPage() {
        Long outletId = 4L;
        Pageable pageable = PageRequest.of(0, 10);
        Page<SaleEntity> entityPage = new PageImpl<>(List.of(sampleEntity), pageable, 1);

        when(jpaSaleRepository.findByOutletId(outletId, pageable)).thenReturn(entityPage);
        when(saleMapper.toDomain(sampleEntity)).thenReturn(sampleDomain);

        Page<SaleDomain> result = saleRepositoryAdapter.findByOutletId(outletId, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(sampleDomain.getId(), result.getContent().get(0).getId());

        verify(jpaSaleRepository, times(1)).findByOutletId(outletId, pageable);
        verify(saleMapper, times(1)).toDomain(sampleEntity);
    }

    @Test
    @DisplayName("findByOutletId should return empty page when no sales exist")
    void findByOutletId_WhenNoSales_ReturnsEmptyPage() {
        Long outletId = 4L;
        Pageable pageable = PageRequest.of(0, 10);
        Page<SaleEntity> emptyPage = new PageImpl<>(List.of(), pageable, 0);

        when(jpaSaleRepository.findByOutletId(outletId, pageable)).thenReturn(emptyPage);

        Page<SaleDomain> result = saleRepositoryAdapter.findByOutletId(outletId, pageable);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        assertEquals(0, result.getTotalElements());
        verify(saleMapper, never()).toDomain(any());
    }

    @Test
    @DisplayName("flexibleSearch should return list of domain objects matching query")
    void flexibleSearch_WhenMatchesFound_ReturnsDomainList() {
        Long outletId = 4L;
        String query = "CLI-001";
        Pageable pageable = PageRequest.of(0, 5);

        when(jpaSaleRepository.flexibleSearchSales(outletId, query, pageable))
                .thenReturn(List.of(sampleEntity));
        when(saleMapper.toDomain(sampleEntity)).thenReturn(sampleDomain);

        List<SaleDomain> result = saleRepositoryAdapter.flexibleSearch(outletId, query, pageable);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(sampleDomain.getId(), result.get(0).getId());

        verify(jpaSaleRepository, times(1)).flexibleSearchSales(outletId, query, pageable);
        verify(saleMapper, times(1)).toDomain(sampleEntity);
    }

    @Test
    @DisplayName("flexibleSearch should return empty list when query produces no matches")
    void flexibleSearch_WhenNoMatches_ReturnsEmptyList() {
        Long outletId = 4L;
        String query = "NON_EXISTENT";
        Pageable pageable = PageRequest.of(0, 5);

        when(jpaSaleRepository.flexibleSearchSales(outletId, query, pageable))
                .thenReturn(List.of());

        List<SaleDomain> result = saleRepositoryAdapter.flexibleSearch(outletId, query, pageable);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(jpaSaleRepository, times(1)).flexibleSearchSales(outletId, query, pageable);
        verify(saleMapper, never()).toDomain(any());
    }

    @Test
    @DisplayName("findRecentSales should return top 20 recent sales mapped to domain")
    void findRecentSales_WhenSalesExist_ReturnsRecentDomainList() {
        Long outletId = 4L;

        when(jpaSaleRepository.findTop20ByOutletIdOrderBySaleDateDesc(outletId))
                .thenReturn(List.of(sampleEntity));
        when(saleMapper.toDomain(sampleEntity)).thenReturn(sampleDomain);

        List<SaleDomain> result = saleRepositoryAdapter.findRecentSales(outletId);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(sampleDomain.getId(), result.get(0).getId());

        verify(jpaSaleRepository, times(1)).findTop20ByOutletIdOrderBySaleDateDesc(outletId);
        verify(saleMapper, times(1)).toDomain(sampleEntity);
    }

    @Test
    @DisplayName("findRecentSales should return empty list when no recent sales found")
    void findRecentSales_WhenNoSales_ReturnsEmptyList() {
        Long outletId = 4L;

        when(jpaSaleRepository.findTop20ByOutletIdOrderBySaleDateDesc(outletId))
                .thenReturn(List.of());

        List<SaleDomain> result = saleRepositoryAdapter.findRecentSales(outletId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(jpaSaleRepository, times(1)).findTop20ByOutletIdOrderBySaleDateDesc(outletId);
        verify(saleMapper, never()).toDomain(any());
    }

    @Test
    @DisplayName("findByOutletAndEmployeeAndDateBetween should return mapped domain list")
    void findByOutletAndEmployeeAndDateBetween_WhenMatchesFound_ReturnsDomainList() {
        Long outletId = 4L;
        Long employeeId = 5L;
        LocalDateTime start = sampleTime.minusDays(1);
        LocalDateTime end = sampleTime.plusDays(1);

        when(jpaSaleRepository.findByOutletIdAndEmployeeIdAndSaleDateBetween(outletId, employeeId, start, end))
                .thenReturn(List.of(sampleEntity));
        when(saleMapper.toDomain(sampleEntity)).thenReturn(sampleDomain);

        List<SaleDomain> result = saleRepositoryAdapter.findByOutletAndEmployeeAndDateBetween(outletId, employeeId, start, end);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(sampleDomain.getId(), result.get(0).getId());

        verify(jpaSaleRepository, times(1)).findByOutletIdAndEmployeeIdAndSaleDateBetween(outletId, employeeId, start, end);
        verify(saleMapper, times(1)).toDomain(sampleEntity);
    }

    @Test
    @DisplayName("findByOutletAndEmployeeAndDateBetween should return empty list when no sales exist")
    void findByOutletAndEmployeeAndDateBetween_WhenNoMatches_ReturnsEmptyList() {
        Long outletId = 4L;
        Long employeeId = 5L;
        LocalDateTime start = sampleTime.minusDays(1);
        LocalDateTime end = sampleTime.plusDays(1);

        when(jpaSaleRepository.findByOutletIdAndEmployeeIdAndSaleDateBetween(outletId, employeeId, start, end))
                .thenReturn(List.of());

        List<SaleDomain> result = saleRepositoryAdapter.findByOutletAndEmployeeAndDateBetween(outletId, employeeId, start, end);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(jpaSaleRepository, times(1)).findByOutletIdAndEmployeeIdAndSaleDateBetween(outletId, employeeId, start, end);
        verify(saleMapper, never()).toDomain(any());
    }

    @Test
    @DisplayName("findByOutletAndDateBetween should return mapped domain list")
    void findByOutletAndDateBetween_WhenMatchesFound_ReturnsDomainList() {
        Long outletId = 4L;
        LocalDateTime start = sampleTime.minusDays(1);
        LocalDateTime end = sampleTime.plusDays(1);

        when(jpaSaleRepository.findByOutletIdAndSaleDateBetween(outletId, start, end))
                .thenReturn(List.of(sampleEntity));
        when(saleMapper.toDomain(sampleEntity)).thenReturn(sampleDomain);

        List<SaleDomain> result = saleRepositoryAdapter.findByOutletAndDateBetween(outletId, start, end);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(sampleDomain.getId(), result.get(0).getId());

        verify(jpaSaleRepository, times(1)).findByOutletIdAndSaleDateBetween(outletId, start, end);
        verify(saleMapper, times(1)).toDomain(sampleEntity);
    }

    @Test
    @DisplayName("findByOutletAndDateBetween should return empty list when no sales exist in date range")
    void findByOutletAndDateBetween_WhenNoMatches_ReturnsEmptyList() {
        Long outletId = 4L;
        LocalDateTime start = sampleTime.minusDays(1);
        LocalDateTime end = sampleTime.plusDays(1);

        when(jpaSaleRepository.findByOutletIdAndSaleDateBetween(outletId, start, end))
                .thenReturn(List.of());

        List<SaleDomain> result = saleRepositoryAdapter.findByOutletAndDateBetween(outletId, start, end);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(jpaSaleRepository, times(1)).findByOutletIdAndSaleDateBetween(outletId, start, end);
        verify(saleMapper, never()).toDomain(any());
    }
}
