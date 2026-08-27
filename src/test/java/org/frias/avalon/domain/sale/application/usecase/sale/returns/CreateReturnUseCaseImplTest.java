package org.frias.avalon.domain.sale.application.usecase.sale.returns;

import org.frias.avalon.core.exeptions.ResourceNotFoundException;
import org.frias.avalon.core.permissions.CurrentUserProviderPort;
import org.frias.avalon.core.permissions.UserContext;
import org.frias.avalon.domain.credit.application.port.CreditRepositoryPort;
import org.frias.avalon.domain.masterdata.domain.model.MasterRoot;
import org.frias.avalon.domain.masterdata.domain.model.MasterTree;
import org.frias.avalon.domain.masterdata.domain.repository.MasterDataRepositoryPort;
import org.frias.avalon.domain.masterdata.domain.service.MasterTreeProvider;
import org.frias.avalon.domain.person.domain.model.PersonDomain;
import org.frias.avalon.domain.person.domain.port.PersonRepositoryPort;
import org.frias.avalon.domain.product.application.port.ProductOutletRepositoryPort;
import org.frias.avalon.domain.product.domain.ProductDomain;
import org.frias.avalon.domain.sale.application.dto.request.CreateReturnRequest;
import org.frias.avalon.domain.sale.application.dto.request.ReturnItemRequest;
import org.frias.avalon.domain.sale.application.dto.response.ReturnResponse;
import org.frias.avalon.domain.sale.application.port.ReturnRepositoryPort;
import org.frias.avalon.domain.sale.application.port.SaleRepositoryPort;
import org.frias.avalon.domain.sale.domain.ReturnDomain;
import org.frias.avalon.domain.sale.domain.ReturnItemDomain;
import org.frias.avalon.domain.sale.domain.SaleDomain;
import org.frias.avalon.domain.sale.domain.SaleItemDomain;
import org.frias.avalon.domain.sale.domain.service.SaleWeightConversionService;
import org.frias.avalon.domain.user.domain.model.UserAvalonDomain;
import org.frias.avalon.domain.user.domain.port.UserAvalonRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@DisplayName("Unit Tests for CreateReturnUseCaseImpl in POS Sales Domain")
class CreateReturnUseCaseImplTest {

    private ReturnRepositoryPort returnRepositoryPort;
    private SaleRepositoryPort saleRepositoryPort;
    private ProductOutletRepositoryPort productOutletRepositoryPort;
    private PersonRepositoryPort personRepositoryPort;
    private UserAvalonRepositoryPort userAvalonRepositoryPort;
    private MasterDataRepositoryPort masterDataRepositoryPort;
    private MasterTreeProvider masterTreeProvider;
    private SaleWeightConversionService weightConversionService;
    private CurrentUserProviderPort currentUserProvider;
    private CreditRepositoryPort creditRepositoryPort;

    private CreateReturnUseCaseImpl createReturnUseCase;

    @BeforeEach
    void setUp() {
        returnRepositoryPort = mock(ReturnRepositoryPort.class);
        saleRepositoryPort = mock(SaleRepositoryPort.class);
        productOutletRepositoryPort = mock(ProductOutletRepositoryPort.class);
        personRepositoryPort = mock(PersonRepositoryPort.class);
        userAvalonRepositoryPort = mock(UserAvalonRepositoryPort.class);
        masterDataRepositoryPort = mock(MasterDataRepositoryPort.class);
        masterTreeProvider = mock(MasterTreeProvider.class);
        weightConversionService = mock(SaleWeightConversionService.class);
        currentUserProvider = mock(CurrentUserProviderPort.class);
        creditRepositoryPort = mock(CreditRepositoryPort.class);

        createReturnUseCase = new CreateReturnUseCaseImpl(
                returnRepositoryPort,
                saleRepositoryPort,
                productOutletRepositoryPort,
                personRepositoryPort,
                userAvalonRepositoryPort,
                masterDataRepositoryPort,
                masterTreeProvider,
                weightConversionService,
                currentUserProvider,
                creditRepositoryPort
        );
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when original sale code does not exist")
    void shouldThrowExceptionWhenSaleNotFound() {
        UUID saleUuid = UUID.randomUUID();
        CreateReturnRequest request = new CreateReturnRequest(
                saleUuid,
                "Defectuoso",
                "Notas",
                "REEMBOLSO",
                List.of(new ReturnItemRequest(10L, "1")),
                false
        );

        when(currentUserProvider.getCurrentUserContext()).thenReturn(new UserContext("cashier1", List.of("ROLE_CJTURNO"), 1L));
        when(currentUserProvider.hasRole(anyString())).thenReturn(false);
        when(saleRepositoryPort.findByCode(saleUuid)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> createReturnUseCase.execute(request));
    }

    @Test
    @DisplayName("Should process return successfully with REEMBOLSO resolution")
    void shouldProcessReturnWithReembolsoSuccessfully() {
        UUID saleUuid = UUID.randomUUID();
        CreateReturnRequest request = new CreateReturnRequest(
                saleUuid,
                "DEFECTO",
                "Devolucion aprobada",
                "REEMBOLSO",
                List.of(new ReturnItemRequest(10L, "2")),
                true
        );

        UserContext userContext = new UserContext("cashier1", List.of("ROLE_CJTURNO"), 1L);
        when(currentUserProvider.getCurrentUserContext()).thenReturn(userContext);
        when(currentUserProvider.hasRole("ROLE_ADMIN")).thenReturn(false);
        when(currentUserProvider.hasRole("ROLE_ADMINTI")).thenReturn(false);
        when(currentUserProvider.getCurrentOutletId()).thenReturn(1L);

        SaleItemDomain saleItem = new SaleItemDomain(
                1L, 10L, 5, "5 UN", new BigDecimal("3500.00"), new BigDecimal("17500.00"), 1L
        );

        SaleDomain originalSale = SaleDomain.fromPersistence(
                100L, saleUuid, new BigDecimal("17500.00"), new BigDecimal("20000.00"), new BigDecimal("2500.00"),
                5L, 1L, 20L, 1L, 15L, LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), List.of(saleItem)
        );

        when(saleRepositoryPort.findByCode(saleUuid)).thenReturn(Optional.of(originalSale));

        UserAvalonDomain userDomain = UserAvalonDomain.fromPersistenceBasic(5L, 15L, "cashier1", 1L);
        when(userAvalonRepositoryPort.findByUserName("cashier1")).thenReturn(Optional.of(userDomain));

        PersonDomain client = PersonDomain.createFromEntity(
                20L, "12345678", "JUAN", "PEREZ", "CALLE 1",
                1L, 1L, 5551234L, "juan@email.com", 1L, LocalDateTime.now(), LocalDateTime.now()
        );

        when(personRepositoryPort.findById(20L)).thenReturn(Optional.of(client));
        when(masterDataRepositoryPort.getIdByCode("DEV")).thenReturn(50L);

        MasterTree masterTree = mock(MasterTree.class);
        when(masterTreeProvider.getTree()).thenReturn(masterTree);

        ProductDomain product = ProductDomain.fromPersistence(
                10L, "Jabon de Tocador", "Jabon 100g", 10, 1L, "", new BigDecimal("3500.00"), 1L, 1L, LocalDateTime.now(), LocalDateTime.now()
        );

        when(productOutletRepositoryPort.findById(10L)).thenReturn(Optional.of(product));
        when(masterTree.getById(1L)).thenReturn(new MasterRoot(1L, "UND", "Unidad", 0L, 1L));
        when(weightConversionService.isWeighable("UND")).thenReturn(false);
        when(weightConversionService.formatFromBaseUnit(2, "UND")).thenReturn("2 UN");

        ReturnItemDomain returnItem = new ReturnItemDomain(
                1L, 10L, 2, "2 UN", new BigDecimal("3500.00"), new BigDecimal("7000.00"), 1L
        );

        ReturnDomain returnDomain = ReturnDomain.create(
                100L, "DEFECTO", "Devolucion aprobada", "REEMBOLSO",
                50L, 15L, 1L, 20L, List.of(returnItem)
        );

        when(returnRepositoryPort.save(any(ReturnDomain.class))).thenReturn(returnDomain);

        ReturnResponse response = createReturnUseCase.execute(request);

        assertNotNull(response);
        assertEquals(saleUuid, response.originalSaleCode());
        assertEquals("REEMBOLSO", response.resolutionType());
        verify(productOutletRepositoryPort, times(1)).save(product);
        verify(returnRepositoryPort, times(1)).save(any(ReturnDomain.class));
    }
}
