package org.frias.avalon.domain.product.application.usecase.changestatus;

import org.frias.avalon.core.exeptions.BusinessException;
import org.frias.avalon.core.exeptions.DomainValidationException;
import org.frias.avalon.core.exeptions.ResourceNotFoundException;
import org.frias.avalon.core.permissions.CurrentUserProviderPort;
import org.frias.avalon.domain.masterdata.domain.model.MasterRoot;
import org.frias.avalon.domain.masterdata.domain.model.MasterTree;
import org.frias.avalon.domain.masterdata.domain.service.MasterTreeProvider;
import org.frias.avalon.domain.outlet.infraestructure.entities.Outlet;
import org.frias.avalon.domain.outlet.infraestructure.repository.JpaOutletRepository;
import org.frias.avalon.domain.product.application.dto.request.ChangeStatusRequest;
import org.frias.avalon.domain.product.application.dto.response.ProductResponse;
import org.frias.avalon.domain.product.application.port.ProductOutletRepositoryPort;
import org.frias.avalon.domain.product.domain.ProductDomain;
import org.frias.avalon.domain.product.infraestructure.mapper.ProductOutletMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas Unitarias para ChangeProductStatusUseCase")
class ChangeProductStatusUseCaseImplTest {

    @Mock
    private ProductOutletRepositoryPort productOutletRepositoryPort;
    @Mock
    private MasterTreeProvider masterTreeProvider;
    @Mock
    private ProductOutletMapper productOutletMapper;
    @Mock
    private CurrentUserProviderPort currentUserProvider;
    @Mock
    private JpaOutletRepository jpaOutletRepository;
    @Mock
    private PlatformTransactionManager transactionManager;

    private ChangeProductStatusUseCaseImpl changeProductStatusUseCase;

    private MasterTree mockMasterTree;
    private MasterRoot mockStatusNode;

    @BeforeEach
    void setUp() {
        TransactionStatus transactionStatus = mock(TransactionStatus.class);
        lenient().when(transactionManager.getTransaction(any())).thenReturn(transactionStatus);

        mockMasterTree = mock(MasterTree.class);
        mockStatusNode = MasterRoot.create("INACT", "Inactivo", 2L, 1L);

        changeProductStatusUseCase = new ChangeProductStatusUseCaseImpl(
                productOutletRepositoryPort,
                masterTreeProvider,
                productOutletMapper,
                currentUserProvider,
                jpaOutletRepository,
                transactionManager
        );
    }

    @Test
    @DisplayName("Debe cambiar el estado del producto exitosamente cuando es un admin global")
    void shouldChangeStatusSuccessfullyForAdmin() {
        Long productId = 10L;
        Long outletId = 1L;
        Long newStatusId = 2L;
        ChangeStatusRequest request = new ChangeStatusRequest(newStatusId);

        ProductDomain existingProduct = ProductDomain.create(
                "Arroz",
                "Arroz blanco",
                10,
                100L,
                null,
                new BigDecimal("3000.00"),
                outletId,
                1L
        );

        when(currentUserProvider.hasRole("ROLE_ADMIN")).thenReturn(true);
        when(currentUserProvider.getCurrentOutletId()).thenReturn(null);

        Outlet mockOutlet = new Outlet();
        mockOutlet.setId(outletId);
        mockOutlet.setCompanyId(10L);
        when(jpaOutletRepository.findAll()).thenReturn(List.of(mockOutlet));

        when(productOutletRepositoryPort.findById(productId)).thenReturn(Optional.of(existingProduct));
        when(masterTreeProvider.getTree()).thenReturn(mockMasterTree);
        when(mockMasterTree.getById(newStatusId)).thenReturn(mockStatusNode);
        when(mockMasterTree.isChildOf(mockStatusNode, "STSGEN")).thenReturn(true);
        when(productOutletRepositoryPort.save(any(ProductDomain.class))).thenReturn(existingProduct);

        ProductResponse expectedResponse = mock(ProductResponse.class);
        when(productOutletMapper.toResponse(existingProduct)).thenReturn(expectedResponse);

        ProductResponse response = changeProductStatusUseCase.execute(productId, request);

        assertNotNull(response);
        assertEquals(expectedResponse, response);
        verify(productOutletRepositoryPort, times(1)).save(existingProduct);
    }

    @Test
    @DisplayName("Debe lanzar DomainValidationException si el estado no es valido")
    void shouldThrowExceptionWhenStatusIsInvalid() {
        Long productId = 10L;
        Long outletId = 1L;
        Long newStatusId = 999L;
        ChangeStatusRequest request = new ChangeStatusRequest(newStatusId);

        ProductDomain existingProduct = ProductDomain.create(
                "Arroz",
                "Arroz blanco",
                10,
                100L,
                null,
                new BigDecimal("3000.00"),
                outletId,
                1L
        );

        when(currentUserProvider.hasRole("ROLE_ADMIN")).thenReturn(true);
        when(currentUserProvider.getCurrentOutletId()).thenReturn(null);

        Outlet mockOutlet = new Outlet();
        mockOutlet.setId(outletId);
        mockOutlet.setCompanyId(10L);
        when(jpaOutletRepository.findAll()).thenReturn(List.of(mockOutlet));

        when(productOutletRepositoryPort.findById(productId)).thenReturn(Optional.of(existingProduct));
        when(masterTreeProvider.getTree()).thenReturn(mockMasterTree);
        when(mockMasterTree.getById(newStatusId)).thenReturn(null);

        assertThrows(DomainValidationException.class, () -> changeProductStatusUseCase.execute(productId, request));
        verify(productOutletRepositoryPort, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar ResourceNotFoundException cuando el producto no se encuentra en ninguna tienda")
    void shouldThrowResourceNotFoundExceptionWhenProductDoesNotExist() {
        Long productId = 999L;
        ChangeStatusRequest request = new ChangeStatusRequest(2L);

        when(currentUserProvider.hasRole("ROLE_ADMIN")).thenReturn(true);
        when(currentUserProvider.getCurrentOutletId()).thenReturn(null);
        when(jpaOutletRepository.findAll()).thenReturn(List.of());

        assertThrows(ResourceNotFoundException.class, () -> changeProductStatusUseCase.execute(productId, request));
    }
}
