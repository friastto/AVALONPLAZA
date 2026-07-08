package org.frias.avalon.domain.sale.application.usecase.sale.create;

import org.frias.avalon.core.exeptions.BusinessException;
import org.frias.avalon.core.permissions.CurrentUserProviderPort;
import org.frias.avalon.core.permissions.UserContext;
import org.frias.avalon.domain.masterdata.domain.model.MasterRoot;
import org.frias.avalon.domain.masterdata.domain.model.MasterTree;
import org.frias.avalon.domain.masterdata.domain.repository.MasterDataRepositoryPort;
import org.frias.avalon.domain.masterdata.domain.service.MasterTreeProvider;
import org.frias.avalon.domain.person.domain.model.PersonDomain;
import org.frias.avalon.domain.person.domain.port.PersonRepositoryPort;
import org.frias.avalon.domain.product.application.port.ProductOutletRepositoryPort;
import org.frias.avalon.domain.product.domain.ProductDomain;
import org.frias.avalon.domain.sale.application.dto.request.CreateSaleRequest;
import org.frias.avalon.domain.sale.application.dto.request.SaleItemRequest;
import org.frias.avalon.domain.sale.application.dto.response.SaleResponse;
import org.frias.avalon.domain.sale.application.port.SaleRepositoryPort;
import org.frias.avalon.domain.sale.domain.SaleDomain;
import org.frias.avalon.domain.sale.domain.service.SaleWeightConversionService;
import org.frias.avalon.domain.user.domain.model.UserAvalonDomain;
import org.frias.avalon.domain.user.domain.port.UserAvalonRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateSaleUseCaseImplTest {

    @Mock private SaleRepositoryPort saleRepositoryPort;
    @Mock private ProductOutletRepositoryPort productOutletRepositoryPort;
    @Mock private PersonRepositoryPort personRepositoryPort;
    @Mock private UserAvalonRepositoryPort userAvalonRepositoryPort;
    @Mock private MasterDataRepositoryPort masterDataRepositoryPort;
    @Mock private MasterTreeProvider masterTreeProvider;
    @Mock private SaleWeightConversionService weightConversionService;
    @Mock private CurrentUserProviderPort currentUserProvider;

    @InjectMocks
    private CreateSaleUseCaseImpl useCase;

    private UserContext userContext;
    private MasterTree masterTree;

    @BeforeEach
    void setUp() {
        userContext = new UserContext("employee_user", List.of("ROLE_CAJTUR"), 4L);
        masterTree = mock(MasterTree.class);
    }

    @Test
    void testExecuteSaleSuccessfully() {
        // Mocking Security Context
        when(currentUserProvider.getCurrentUserContext()).thenReturn(userContext);
        when(currentUserProvider.getCurrentOutletId()).thenReturn(4L);
        when(currentUserProvider.hasRole(anyString())).thenReturn(false); // No es admin global

        // Mocking MasterTree and Roles
        when(masterTreeProvider.getTree()).thenReturn(masterTree);
        MasterRoot roleNode = new MasterRoot(10L, "CAJTUR", "Cajero Turno", 100L, 1L);
        when(masterTree.getByCode("CAJTUR")).thenReturn(roleNode);
        when(masterTree.isChildOf(roleNode, "OPERACION")).thenReturn(true);

        // Mocking Employee resolving
        UserAvalonDomain user = UserAvalonDomain.fromPersistenceBasic(1L, 20L, "employee_user", 1L);
        when(userAvalonRepositoryPort.findByUserName("employee_user")).thenReturn(Optional.of(user));

        // Mocking Client resolving
        PersonDomain client = PersonDomain.createFromEntity(
                30L, "123456", "Juan", "Perez", "Calle 1",
                1L, 1L, 123456L, "juan@email.com", 1L, LocalDateTime.now(), LocalDateTime.now()
        );
        when(personRepositoryPort.findByNumberid("123456")).thenReturn(Optional.of(client));

        // Mocking status resolved
        when(masterDataRepositoryPort.getIdByCode("ACT")).thenReturn(5L);

        // Mocking Product resolving
        ProductDomain product = ProductDomain.fromPersistence(
                50L, "Arroz Premium", "Arroz desc", 10000, 11L, "img",
                new BigDecimal("5.0"), 4L, 1L, LocalDateTime.now(), LocalDateTime.now()
        );
        when(productOutletRepositoryPort.findById(50L)).thenReturn(Optional.of(product));

        // Mocking Product Unit and Weight Conversion
        MasterRoot unitNode = new MasterRoot(11L, "KG", "Kilogramos", 200L, 1L);
        when(masterTree.getById(11L)).thenReturn(unitNode);
        when(weightConversionService.isWeighable("KG")).thenReturn(true);
        when(weightConversionService.convertToBaseUnit(new BigDecimal("1.5"), "KG")).thenReturn(1500);
        when(weightConversionService.formatFromBaseUnit(1500, "KG")).thenReturn("1.5 KG");

        // Mocking Sale saving
        SaleDomain mockSavedSale = SaleDomain.fromPersistence(
                100L, UUID.randomUUID(), new BigDecimal("7.5"), new BigDecimal("10"), new BigDecimal("2.5"),
                8L, 5L, 30L, 4L, 20L, LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), List.of()
        );
        when(saleRepositoryPort.save(any(SaleDomain.class))).thenReturn(mockSavedSale);

        // Mocking MasterTree nodes for Response mapping
        MasterRoot payNode = new MasterRoot(8L, "EFE", "Efectivo", 300L, 1L);
        MasterRoot statusNode = new MasterRoot(5L, "ACT", "Activo", 400L, 1L);
        when(masterTree.getById(8L)).thenReturn(payNode);
        when(masterTree.getById(5L)).thenReturn(statusNode);

        // Request definition
        CreateSaleRequest request = new CreateSaleRequest(
                "123456", 4L, 8L, new BigDecimal("10"),
                List.of(new SaleItemRequest(50L, "1.5"))
        );

        SaleResponse response = useCase.execute(request);

        assertNotNull(response);
        assertEquals(100L, response.id());
        assertEquals(new BigDecimal("7.5"), response.totalAmount());
        assertEquals(new BigDecimal("2.5"), response.changeGiven());
        assertEquals("Juan Perez", response.clientFullName());

        verify(productOutletRepositoryPort, times(1)).save(any(ProductDomain.class));
        verify(saleRepositoryPort, times(1)).save(any(SaleDomain.class));
    }

    @Test
    void testExecuteSaleTenantDenyThrowsException() {
        when(currentUserProvider.getCurrentUserContext()).thenReturn(userContext);
        when(currentUserProvider.getCurrentOutletId()).thenReturn(4L);
        when(currentUserProvider.hasRole(anyString())).thenReturn(false);

        // Requesting for outlet 5L, but current employee is tied to 4L
        CreateSaleRequest request = new CreateSaleRequest(
                "123456", 5L, 8L, new BigDecimal("10"),
                List.of(new SaleItemRequest(50L, "1.5"))
        );

        assertThrows(BusinessException.class, () -> useCase.execute(request));
    }
}
