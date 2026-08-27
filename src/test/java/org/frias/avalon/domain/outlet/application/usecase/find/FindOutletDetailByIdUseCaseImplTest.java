package org.frias.avalon.domain.outlet.application.usecase.find;

import org.frias.avalon.core.exeptions.ResourceNotFoundException;
import org.frias.avalon.core.tenant.TenantContext;
import org.frias.avalon.domain.masterdata.application.dto.response.StatusResponseDto;
import org.frias.avalon.domain.outlet.application.dto.LocationDto;
import org.frias.avalon.domain.outlet.application.dto.response.OutletDetailResponse;
import org.frias.avalon.domain.outlet.application.dto.response.OutletResponseDto;
import org.frias.avalon.domain.outlet.domain.model.LocationDomain;
import org.frias.avalon.domain.outlet.domain.model.OutletDomain;
import org.frias.avalon.domain.outlet.domain.port.OutletRepositoryPort;
import org.frias.avalon.domain.outlet.infraestructure.mapper.OutletMapper;
import org.frias.avalon.domain.product.application.dto.response.ProductResponse;
import org.frias.avalon.domain.product.application.port.ProductOutletRepositoryPort;
import org.frias.avalon.domain.product.domain.ProductDomain;
import org.frias.avalon.domain.product.infraestructure.mapper.ProductOutletMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas Unitarias para FindOutletDetailByIdUseCaseImpl")
class FindOutletDetailByIdUseCaseImplTest {

    @Mock
    private OutletRepositoryPort outletRepository;

    @Mock
    private ProductOutletRepositoryPort productRepository;

    @Mock
    private OutletMapper outletMapper;

    @Mock
    private ProductOutletMapper productMapper;

    @Mock
    private PlatformTransactionManager transactionManager;

    private FindOutletDetailByIdUseCaseImpl findOutletDetailByIdUseCase;

    @BeforeEach
    void setUp() {
        TransactionStatus status = mock(TransactionStatus.class);
        lenient().when(transactionManager.getTransaction(any())).thenReturn(status);

        findOutletDetailByIdUseCase = new FindOutletDetailByIdUseCaseImpl(
                outletRepository,
                productRepository,
                outletMapper,
                productMapper,
                transactionManager
        );
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("Deberia retornar el detalle de la tienda con productos exitosamente cuando la tienda tiene companyId")
    void shouldReturnOutletDetailWithProductsSuccessfullyWhenCompanyIdIsPresent() {
        // Arrange
        Long outletId = 1L;
        Long companyId = 50L;
        LocationDomain locationDomain = new LocationDomain(4.60971, -74.08175);
        LocationDto locationDto = new LocationDto(4.60971, -74.08175);
        StatusResponseDto statusResponseDto = new StatusResponseDto(1L, "ACT", "Activo");

        OutletDomain outletDomain = OutletDomain.fromPersistence(
                outletId, "OUT-001", "Tienda Principal", "Calle 100", "3001234567",
                "900123456-1", 1L, locationDomain, BigDecimal.ZERO, false, BigDecimal.ZERO, companyId, LocalDateTime.now(), null
        );

        OutletResponseDto baseOutletDto = new OutletResponseDto(
                outletId, "OUT-001", "Tienda Principal", "Calle 100", "3001234567",
                "900123456-1", locationDto, statusResponseDto, companyId, false, BigDecimal.ZERO
        );

        ProductDomain productDomain = ProductDomain.fromPersistence(
                10L, "Producto 1", "P001", 100, 1L, "sku-001", new BigDecimal("5000.00"), 1L, 1L, LocalDateTime.now(), null
        );

        ProductResponse productResponse = new ProductResponse(
                10L, "Producto 1", "P001", "100 UND", "0 UND", "0 UND", "sku-001", "", new BigDecimal("5000.00"), 1L, null, null, 1L, LocalDateTime.now(), null
        );

        given(outletRepository.findById(outletId)).willReturn(Optional.of(outletDomain));
        given(productRepository.findAll(eq(null), eq(outletId), any(Pageable.class)))
                .willReturn(new PageImpl<>(List.of(productDomain)));
        given(productMapper.toResponse(productDomain)).willReturn(productResponse);
        given(outletMapper.toResponse(outletDomain)).willReturn(baseOutletDto);

        // Act
        OutletDetailResponse response = findOutletDetailByIdUseCase.execute(outletId);

        // Assert
        assertNotNull(response);
        assertEquals(outletId, response.id());
        assertEquals(companyId, response.companyId());
        assertEquals("Tienda Principal", response.name());
        assertEquals(1, response.catalog().size());
        assertEquals(productResponse, response.catalog().get(0));

        assertNull(TenantContext.getTenantId());
        assertNull(TenantContext.getTenantOutletId());
    }

    @Test
    @DisplayName("Deberia retornar el detalle de la tienda exitosamente cuando companyId es nulo")
    void shouldReturnOutletDetailWithProductsSuccessfullyWhenCompanyIdIsNull() {
        // Arrange
        Long outletId = 2L;
        LocationDomain locationDomain = new LocationDomain(6.25184, -75.56359);
        LocationDto locationDto = new LocationDto(6.25184, -75.56359);

        OutletDomain outletDomain = OutletDomain.fromPersistence(
                outletId, "OUT-002", "Tienda Norte", "Carrera 43A", "3119876543",
                "900987654-2", 1L, locationDomain, BigDecimal.ZERO, false, BigDecimal.ZERO, null, LocalDateTime.now(), null
        );

        OutletResponseDto baseOutletDto = new OutletResponseDto(
                outletId, "OUT-002", "Tienda Norte", "Carrera 43A", "3119876543",
                "900987654-2", locationDto, null, null, false, BigDecimal.ZERO
        );

        given(outletRepository.findById(outletId)).willReturn(Optional.of(outletDomain));
        given(productRepository.findAll(eq(null), eq(outletId), any(Pageable.class)))
                .willReturn(new PageImpl<>(Collections.emptyList()));
        given(outletMapper.toResponse(outletDomain)).willReturn(baseOutletDto);

        // Act
        OutletDetailResponse response = findOutletDetailByIdUseCase.execute(outletId);

        // Assert
        assertNotNull(response);
        assertEquals(outletId, response.id());
        assertNull(response.companyId());
        assertTrue(response.catalog().isEmpty());

        assertNull(TenantContext.getTenantId());
        assertNull(TenantContext.getTenantOutletId());
    }

    @Test
    @DisplayName("Deberia lanzar ResourceNotFoundException cuando la tienda no existe")
    void shouldThrowResourceNotFoundExceptionWhenOutletDoesNotExist() {
        // Arrange
        Long outletId = 99L;
        given(outletRepository.findById(outletId)).willReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> findOutletDetailByIdUseCase.execute(outletId)
        );

        assertEquals("Outlet not found with id: 99", exception.getMessage());
        verifyNoInteractions(productRepository);
        verifyNoInteractions(productMapper);
        verifyNoInteractions(outletMapper);
    }

    @Test
    @DisplayName("Deberia retornar lista de productos vacia cuando ocurre una excepcion al consultar productos")
    void shouldReturnEmptyProductListWhenProductRepositoryThrowsException() {
        // Arrange
        Long outletId = 1L;
        LocationDomain locationDomain = new LocationDomain(4.60971, -74.08175);
        LocationDto locationDto = new LocationDto(4.60971, -74.08175);

        OutletDomain outletDomain = OutletDomain.fromPersistence(
                outletId, "OUT-001", "Tienda Principal", "Calle 100", "3001234567",
                "900123456-1", 1L, locationDomain, BigDecimal.ZERO, false, BigDecimal.ZERO, 50L, LocalDateTime.now(), null
        );

        OutletResponseDto baseOutletDto = new OutletResponseDto(
                outletId, "OUT-001", "Tienda Principal", "Calle 100", "3001234567",
                "900123456-1", locationDto, null, 50L, false, BigDecimal.ZERO
        );

        given(outletRepository.findById(outletId)).willReturn(Optional.of(outletDomain));
        given(productRepository.findAll(eq(null), eq(outletId), any(Pageable.class)))
                .willThrow(new RuntimeException("Error inesperado en BD"));
        given(outletMapper.toResponse(outletDomain)).willReturn(baseOutletDto);

        // Act
        OutletDetailResponse response = findOutletDetailByIdUseCase.execute(outletId);

        // Assert
        assertNotNull(response);
        assertEquals(outletId, response.id());
        assertTrue(response.catalog().isEmpty());

        assertNull(TenantContext.getTenantId());
        assertNull(TenantContext.getTenantOutletId());
    }
}
