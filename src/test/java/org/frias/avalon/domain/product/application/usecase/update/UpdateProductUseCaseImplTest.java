package org.frias.avalon.domain.product.application.usecase.update;

import org.frias.avalon.core.exeptions.BusinessException;
import org.frias.avalon.core.exeptions.ResourceNotFoundException;
import org.frias.avalon.core.permissions.CurrentUserProviderPort;
import org.frias.avalon.domain.masterdata.domain.model.MasterRoot;
import org.frias.avalon.domain.masterdata.domain.model.MasterTree;
import org.frias.avalon.domain.masterdata.domain.service.MasterTreeProvider;
import org.frias.avalon.domain.outlet.infraestructure.entities.Outlet;
import org.frias.avalon.domain.outlet.infraestructure.repository.JpaOutletRepository;
import org.frias.avalon.domain.product.application.dto.request.ProductUpdateRequest;
import org.frias.avalon.domain.product.application.dto.response.ProductResponse;
import org.frias.avalon.domain.product.application.port.ProductOutletRepositoryPort;
import org.frias.avalon.domain.product.application.service.QuantityParserService;
import org.frias.avalon.domain.product.domain.ProductDomain;
import org.frias.avalon.domain.product.domain.service.UnitConversionService;
import org.frias.avalon.domain.product.infraestructure.mapper.ProductOutletMapper;
import org.frias.avalon.domain.product.presentation.ProductWebSocketPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas Unitarias para UpdateProductUseCase")
class UpdateProductUseCaseImplTest {

    @Mock
    private ProductOutletRepositoryPort productOutletRepositoryPort;
    @Mock
    private MasterTreeProvider masterTreeProvider;
    @Mock
    private QuantityParserService quantityParserService;
    @Mock
    private UnitConversionService unitConversionService;
    @Mock
    private ProductOutletMapper productOutletMapper;
    @Mock
    private CurrentUserProviderPort currentUserProvider;
    @Mock
    private ProductWebSocketPublisher productWebSocketPublisher;
    @Mock
    private JpaOutletRepository jpaOutletRepository;
    @Mock
    private PlatformTransactionManager transactionManager;

    private UpdateProductUseCaseImpl updateProductUseCase;

    private MasterTree mockMasterTree;
    private MasterRoot mockUnitNode;

    @BeforeEach
    void setUp() {
        TransactionStatus transactionStatus = mock(TransactionStatus.class);
        lenient().when(transactionManager.getTransaction(any())).thenReturn(transactionStatus);

        mockMasterTree = mock(MasterTree.class);
        mockUnitNode = MasterRoot.create("UND", "UNIDAD", 100L, 1L);

        updateProductUseCase = new UpdateProductUseCaseImpl(
                productOutletRepositoryPort,
                masterTreeProvider,
                quantityParserService,
                unitConversionService,
                productOutletMapper,
                currentUserProvider,
                productWebSocketPublisher,
                jpaOutletRepository,
                transactionManager
        );
    }

    @Test
    @DisplayName("Debe actualizar el stock y emitir notificacion WebSocket al guardar")
    void shouldUpdateStockAndBroadcastWebSocketEvent() {
        Long productId = 10L;
        Long outletId = 1L;
        ProductUpdateRequest request = new ProductUpdateRequest(
                "Arroz Diana",
                "Arroz blanco 1000g",
                "15",
                200L,
                "https://cdn.example.com/arroz.jpg",
                new BigDecimal("4500.00")
        );

        ProductDomain existingProduct = ProductDomain.create(
                "Arroz Diana",
                "Arroz blanco",
                5,
                200L,
                "https://cdn.example.com/arroz.jpg",
                new BigDecimal("4500.00"),
                outletId,
                1L
        );

        ProductResponse expectedResponse = new ProductResponse(
                productId,
                "Arroz Diana",
                "Arroz blanco 1000g",
                "15 UND",
                "0.0 UND",
                "0.0 UND",
                "https://cdn.example.com/arroz.jpg",
                "https://cdn.example.com/arroz.jpg",
                new BigDecimal("4500.00"),
                outletId,
                null,
                "7701234567890",
                200L,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(currentUserProvider.hasRole("ROLE_ADMIN")).thenReturn(true);
        when(currentUserProvider.getCurrentOutletId()).thenReturn(null);

        Outlet mockOutlet = new Outlet();
        mockOutlet.setId(outletId);
        mockOutlet.setCompanyId(10L);
        when(jpaOutletRepository.findAll()).thenReturn(List.of(mockOutlet));

        when(productOutletRepositoryPort.findById(productId)).thenReturn(Optional.of(existingProduct));
        when(quantityParserService.parseAndValidate("15")).thenReturn(new BigDecimal("15"));
        when(masterTreeProvider.getTree()).thenReturn(mockMasterTree);
        when(mockMasterTree.getById(200L)).thenReturn(mockUnitNode);
        when(mockMasterTree.isChildOf(mockUnitNode, "UNIT")).thenReturn(true);
        when(unitConversionService.convertToSmallestUnit(new BigDecimal("15"), "UND")).thenReturn(15);
        when(productOutletRepositoryPort.save(any(ProductDomain.class))).thenReturn(existingProduct);
        when(productOutletMapper.toResponse(existingProduct)).thenReturn(expectedResponse);

        ProductResponse response = updateProductUseCase.execute(productId, request);

        assertNotNull(response);
        assertEquals("15 UND", response.displayStock());
        verify(productWebSocketPublisher, times(1)).broadcastProductStockChanged(eq(outletId), eq(expectedResponse));
    }

    @Test
    @DisplayName("Debe lanzar ResourceNotFoundException cuando el producto no existe")
    void shouldThrowExceptionWhenProductNotFound() {
        Long productId = 999L;
        ProductUpdateRequest request = new ProductUpdateRequest(
                "Panela",
                "Panela redonda",
                "10",
                200L,
                null,
                new BigDecimal("2000.00")
        );

        when(currentUserProvider.hasRole("ROLE_ADMIN")).thenReturn(true);
        when(currentUserProvider.getCurrentOutletId()).thenReturn(null);
        when(jpaOutletRepository.findAll()).thenReturn(List.of());

        assertThrows(ResourceNotFoundException.class, () -> updateProductUseCase.execute(productId, request));
        verify(productWebSocketPublisher, never()).broadcastProductStockChanged(any(), any());
    }

    @Test
    @DisplayName("Debe denegar acceso cuando el empleado no pertenece a la misma tienda")
    void shouldThrowBusinessExceptionWhenTenantOutletMismatch() {
        Long productId = 10L;
        ProductUpdateRequest request = new ProductUpdateRequest(
                "Arroz",
                null,
                "5",
                200L,
                null,
                new BigDecimal("3000.00")
        );

        ProductDomain productOtherOutlet = ProductDomain.create(
                "Arroz",
                null,
                5,
                200L,
                null,
                new BigDecimal("3000.00"),
                2L,
                1L
        );

        when(currentUserProvider.hasRole("ROLE_ADMIN")).thenReturn(false);
        when(currentUserProvider.hasRole("ROLE_ADMINTI")).thenReturn(false);
        when(currentUserProvider.hasRole("ROLE_GERGEN")).thenReturn(false);
        when(currentUserProvider.getCurrentOutletId()).thenReturn(1L);

        Outlet employeeOutlet = new Outlet();
        employeeOutlet.setId(1L);
        employeeOutlet.setCompanyId(10L);
        when(jpaOutletRepository.findById(1L)).thenReturn(Optional.of(employeeOutlet));
        when(productOutletRepositoryPort.findById(productId)).thenReturn(Optional.of(productOtherOutlet));

        assertThrows(BusinessException.class, () -> updateProductUseCase.execute(productId, request));
        verify(productWebSocketPublisher, never()).broadcastProductStockChanged(any(), any());
    }
}
