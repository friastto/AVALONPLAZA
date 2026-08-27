package org.frias.avalon.domain.sale.application.usecase.sale.returns;

import org.frias.avalon.core.exeptions.BusinessException;
import org.frias.avalon.core.exeptions.ResourceNotFoundException;
import org.frias.avalon.core.permissions.CurrentUserProviderPort;
import org.frias.avalon.core.permissions.UserContext;
import org.frias.avalon.domain.credit.application.port.CreditRepositoryPort;
import org.frias.avalon.domain.credit.domain.model.CreditAccountDomain;
import org.frias.avalon.domain.credit.domain.model.CreditTransactionDomain;
import org.frias.avalon.domain.masterdata.domain.model.MasterRoot;
import org.frias.avalon.domain.masterdata.domain.model.MasterTree;
import org.frias.avalon.domain.masterdata.domain.repository.MasterDataRepositoryPort;
import org.frias.avalon.domain.masterdata.domain.service.MasterTreeProvider;
import org.frias.avalon.domain.person.domain.model.PersonDomain;
import org.frias.avalon.domain.person.domain.port.PersonRepositoryPort;
import org.frias.avalon.domain.product.application.port.ProductOutletRepositoryPort;
import org.frias.avalon.domain.product.domain.ProductDomain;
import org.frias.avalon.domain.sale.application.dto.request.CreateExchangeRequest;
import org.frias.avalon.domain.sale.application.dto.request.ExchangeItemRequest;
import org.frias.avalon.domain.sale.application.dto.request.ReturnItemRequest;
import org.frias.avalon.domain.sale.application.dto.response.ExchangeResponse;
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
import static org.mockito.Mockito.*;

@DisplayName("Unit Tests for CreateExchangeUseCaseImpl in POS Sales Domain")
class CreateExchangeUseCaseImplTest {

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

    private MasterTree masterTree;
    private CreateExchangeUseCaseImpl createExchangeUseCase;

    private final UUID saleUuid = UUID.randomUUID();
    private final Long outletId = 1L;
    private final Long clientId = 20L;
    private final Long employeePersonId = 15L;

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

        masterTree = mock(MasterTree.class);
        when(masterTreeProvider.getTree()).thenReturn(masterTree);

        createExchangeUseCase = new CreateExchangeUseCaseImpl(
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

    // ==========================================
    // HELPER METHODS FOR SETUP
    // ==========================================

    private UserContext setupUserContext(String username, String role, Long userOutletId) {
        UserContext userContext = new UserContext(username, List.of(role), userOutletId);
        when(currentUserProvider.getCurrentUserContext()).thenReturn(userContext);
        when(currentUserProvider.hasRole("ROLE_ADMIN")).thenReturn("ROLE_ADMIN".equals(role));
        when(currentUserProvider.hasRole("ROLE_ADMINTI")).thenReturn("ROLE_ADMINTI".equals(role));
        when(currentUserProvider.getCurrentOutletId()).thenReturn(userOutletId);

        UserAvalonDomain userDomain = UserAvalonDomain.fromPersistenceBasic(5L, employeePersonId, username, userOutletId);
        when(userAvalonRepositoryPort.findByUserName(username)).thenReturn(Optional.of(userDomain));

        return userContext;
    }

    private SaleDomain createOriginalSale(Long saleId, Long outletId, Long clientId, SaleItemDomain item) {
        return SaleDomain.fromPersistence(
                saleId, saleUuid, item.getSubtotal(), item.getSubtotal(), BigDecimal.ZERO,
                1L, 1L, clientId, outletId, employeePersonId, LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), List.of(item)
        );
    }

    private PersonDomain createClient(Long clientId) {
        return PersonDomain.createFromEntity(
                clientId, "12345678", "JUAN", "PEREZ", "CALLE 1",
                1L, 1L, 5551234L, "juan@email.com", 1L, LocalDateTime.now(), LocalDateTime.now()
        );
    }

    private ProductDomain createProduct(Long productId, String name, int stock, BigDecimal price, Long productOutletId, Long unitMeasureId) {
        return ProductDomain.fromPersistence(
                productId, name, name, stock, 1L, "", price, productOutletId, unitMeasureId, LocalDateTime.now(), LocalDateTime.now()
        );
    }

    private void setupCommonMasterData() {
        when(masterDataRepositoryPort.getIdByCode("ACT")).thenReturn(1L);
        when(masterDataRepositoryPort.getIdByCode("DEV")).thenReturn(50L);

        MasterRoot cashNode = new MasterRoot(1L, "EFE", "Efectivo", 0L, 1L);
        MasterRoot fiadoNode = new MasterRoot(2L, "FIA", "Fiado", 0L, 1L);
        MasterRoot statusNode = new MasterRoot(1L, "ACT", "Activo", 0L, 1L);

        when(masterTree.getById(1L)).thenReturn(cashNode);
        when(masterTree.getById(2L)).thenReturn(fiadoNode);
        when(masterTree.getById(100L)).thenReturn(statusNode);
    }

    // ==========================================
    // ERROR HANDLING & VALIDATION TESTS
    // ==========================================

    @Test
    @DisplayName("Should throw ResourceNotFoundException when original sale code does not exist")
    void shouldThrowExceptionWhenSaleNotFound() {
        CreateExchangeRequest request = new CreateExchangeRequest(
                saleUuid, "DEFECTO", "Notas",
                List.of(new ReturnItemRequest(10L, "1")),
                List.of(new ExchangeItemRequest(12L, "1")),
                1L, new BigDecimal("5000.00"), false
        );

        when(currentUserProvider.getCurrentUserContext()).thenReturn(new UserContext("cashier1", List.of("ROLE_CJTURNO"), outletId));
        when(currentUserProvider.hasRole(anyString())).thenReturn(false);
        when(saleRepositoryPort.findByCode(saleUuid)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> createExchangeUseCase.execute(request));
    }

    @Test
    @DisplayName("Should throw BusinessException when sale belongs to another outlet and user is not system admin")
    void shouldThrowExceptionWhenOutletMismatchAndNotAdmin() {
        CreateExchangeRequest request = new CreateExchangeRequest(
                saleUuid, "DEFECTO", "Notas",
                List.of(new ReturnItemRequest(10L, "1")),
                List.of(new ExchangeItemRequest(12L, "1")),
                1L, BigDecimal.ZERO, false
        );

        when(currentUserProvider.getCurrentUserContext()).thenReturn(new UserContext("cashier1", List.of("ROLE_CJTURNO"), 1L));
        when(currentUserProvider.hasRole("ROLE_ADMIN")).thenReturn(false);
        when(currentUserProvider.hasRole("ROLE_ADMINTI")).thenReturn(false);
        when(currentUserProvider.getCurrentOutletId()).thenReturn(1L);

        SaleItemDomain item = new SaleItemDomain(1L, 10L, 1, "1 UN", new BigDecimal("5000"), new BigDecimal("5000"), 1L);
        SaleDomain originalSaleOtherOutlet = createOriginalSale(100L, 2L, clientId, item);

        when(saleRepositoryPort.findByCode(saleUuid)).thenReturn(Optional.of(originalSaleOtherOutlet));

        BusinessException ex = assertThrows(BusinessException.class, () -> createExchangeUseCase.execute(request));
        assertEquals("Acceso denegado: La venta pertenece a otra tienda.", ex.getMessage());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when authenticated user is not found")
    void shouldThrowExceptionWhenUserNotFound() {
        CreateExchangeRequest request = new CreateExchangeRequest(
                saleUuid, "DEFECTO", "Notas",
                List.of(new ReturnItemRequest(10L, "1")),
                List.of(new ExchangeItemRequest(12L, "1")),
                1L, BigDecimal.ZERO, false
        );

        setupUserContext("unknownUser", "ROLE_CJTURNO", outletId);
        when(userAvalonRepositoryPort.findByUserName("unknownUser")).thenReturn(Optional.empty());

        SaleItemDomain item = new SaleItemDomain(1L, 10L, 1, "1 UN", new BigDecimal("5000"), new BigDecimal("5000"), 1L);
        SaleDomain originalSale = createOriginalSale(100L, outletId, clientId, item);
        when(saleRepositoryPort.findByCode(saleUuid)).thenReturn(Optional.of(originalSale));

        assertThrows(ResourceNotFoundException.class, () -> createExchangeUseCase.execute(request));
    }

    @Test
    @DisplayName("Should throw BusinessException when current user has no employee ID")
    void shouldThrowExceptionWhenEmployeeIdIsNull() {
        CreateExchangeRequest request = new CreateExchangeRequest(
                saleUuid, "DEFECTO", "Notas",
                List.of(new ReturnItemRequest(10L, "1")),
                List.of(new ExchangeItemRequest(12L, "1")),
                1L, BigDecimal.ZERO, false
        );

        when(currentUserProvider.getCurrentUserContext()).thenReturn(new UserContext("cashier1", List.of("ROLE_CJTURNO"), outletId));
        UserAvalonDomain userWithoutEmployee = UserAvalonDomain.fromPersistenceBasic(5L, null, "cashier1", outletId);
        when(userAvalonRepositoryPort.findByUserName("cashier1")).thenReturn(Optional.of(userWithoutEmployee));

        SaleItemDomain item = new SaleItemDomain(1L, 10L, 1, "1 UN", new BigDecimal("5000"), new BigDecimal("5000"), 1L);
        SaleDomain originalSale = createOriginalSale(100L, outletId, clientId, item);
        when(saleRepositoryPort.findByCode(saleUuid)).thenReturn(Optional.of(originalSale));

        BusinessException ex = assertThrows(BusinessException.class, () -> createExchangeUseCase.execute(request));
        assertEquals("El usuario actual no tiene un registro de empleado asociado.", ex.getMessage());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when client is not found")
    void shouldThrowExceptionWhenClientNotFound() {
        CreateExchangeRequest request = new CreateExchangeRequest(
                saleUuid, "DEFECTO", "Notas",
                List.of(new ReturnItemRequest(10L, "1")),
                List.of(new ExchangeItemRequest(12L, "1")),
                1L, BigDecimal.ZERO, false
        );

        setupUserContext("cashier1", "ROLE_CJTURNO", outletId);
        SaleItemDomain item = new SaleItemDomain(1L, 10L, 1, "1 UN", new BigDecimal("5000"), new BigDecimal("5000"), 1L);
        SaleDomain originalSale = createOriginalSale(100L, outletId, clientId, item);
        when(saleRepositoryPort.findByCode(saleUuid)).thenReturn(Optional.of(originalSale));
        when(personRepositoryPort.findById(clientId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> createExchangeUseCase.execute(request));
    }

    @Test
    @DisplayName("Should throw IllegalStateException when DEV status code is missing in MasterData")
    void shouldThrowExceptionWhenDevStatusIdIsNull() {
        CreateExchangeRequest request = new CreateExchangeRequest(
                saleUuid, "DEFECTO", "Notas",
                List.of(new ReturnItemRequest(10L, "1")),
                List.of(new ExchangeItemRequest(12L, "1")),
                1L, BigDecimal.ZERO, false
        );

        setupUserContext("cashier1", "ROLE_CJTURNO", outletId);
        SaleItemDomain item = new SaleItemDomain(1L, 10L, 1, "1 UN", new BigDecimal("5000"), new BigDecimal("5000"), 1L);
        SaleDomain originalSale = createOriginalSale(100L, outletId, clientId, item);
        when(saleRepositoryPort.findByCode(saleUuid)).thenReturn(Optional.of(originalSale));
        when(personRepositoryPort.findById(clientId)).thenReturn(Optional.of(createClient(clientId)));

        when(masterDataRepositoryPort.getIdByCode("ACT")).thenReturn(1L);
        when(masterDataRepositoryPort.getIdByCode("DEV")).thenReturn(null);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> createExchangeUseCase.execute(request));
        assertEquals("Estado 'DEV' (Devuelto) no encontrado en MasterData.", ex.getMessage());
    }

    @Test
    @DisplayName("Should throw BusinessException when returned item was not in original sale")
    void shouldThrowExceptionWhenReturnedItemNotInOriginalSale() {
        CreateExchangeRequest request = new CreateExchangeRequest(
                saleUuid, "DEFECTO", "Notas",
                List.of(new ReturnItemRequest(99L, "1")),
                List.of(new ExchangeItemRequest(12L, "1")),
                1L, BigDecimal.ZERO, false
        );

        setupUserContext("cashier1", "ROLE_CJTURNO", outletId);
        setupCommonMasterData();
        SaleItemDomain item = new SaleItemDomain(1L, 10L, 1, "1 UN", new BigDecimal("5000"), new BigDecimal("5000"), 1L);
        SaleDomain originalSale = createOriginalSale(100L, outletId, clientId, item);
        when(saleRepositoryPort.findByCode(saleUuid)).thenReturn(Optional.of(originalSale));
        when(personRepositoryPort.findById(clientId)).thenReturn(Optional.of(createClient(clientId)));

        BusinessException ex = assertThrows(BusinessException.class, () -> createExchangeUseCase.execute(request));
        assertTrue(ex.getMessage().contains("no está en la venta original"));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when returned product does not exist in outlet repository")
    void shouldThrowExceptionWhenReturnedProductNotFound() {
        CreateExchangeRequest request = new CreateExchangeRequest(
                saleUuid, "DEFECTO", "Notas",
                List.of(new ReturnItemRequest(10L, "1")),
                List.of(new ExchangeItemRequest(12L, "1")),
                1L, BigDecimal.ZERO, false
        );

        setupUserContext("cashier1", "ROLE_CJTURNO", outletId);
        setupCommonMasterData();
        SaleItemDomain item = new SaleItemDomain(1L, 10L, 1, "1 UN", new BigDecimal("5000"), new BigDecimal("5000"), 1L);
        SaleDomain originalSale = createOriginalSale(100L, outletId, clientId, item);
        when(saleRepositoryPort.findByCode(saleUuid)).thenReturn(Optional.of(originalSale));
        when(personRepositoryPort.findById(clientId)).thenReturn(Optional.of(createClient(clientId)));
        when(productOutletRepositoryPort.findById(10L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> createExchangeUseCase.execute(request));
    }

    @Test
    @DisplayName("Should throw BusinessException when returned weighable product quantity is invalid decimal")
    void shouldThrowExceptionWhenReturnedWeighableQtyInvalidDecimal() {
        CreateExchangeRequest request = new CreateExchangeRequest(
                saleUuid, "DEFECTO", "Notas",
                List.of(new ReturnItemRequest(10L, "abc")),
                List.of(new ExchangeItemRequest(12L, "1")),
                1L, BigDecimal.ZERO, false
        );

        setupUserContext("cashier1", "ROLE_CJTURNO", outletId);
        setupCommonMasterData();
        SaleItemDomain item = new SaleItemDomain(1L, 10L, 1000, "1.000 KG", new BigDecimal("5000"), new BigDecimal("5000"), 2L);
        SaleDomain originalSale = createOriginalSale(100L, outletId, clientId, item);
        when(saleRepositoryPort.findByCode(saleUuid)).thenReturn(Optional.of(originalSale));
        when(personRepositoryPort.findById(clientId)).thenReturn(Optional.of(createClient(clientId)));

        ProductDomain product = createProduct(10L, "Carne", 1000, new BigDecimal("5000"), outletId, 2L);
        when(productOutletRepositoryPort.findById(10L)).thenReturn(Optional.of(product));
        when(masterTree.getById(2L)).thenReturn(new MasterRoot(2L, "KG", "Kilogramo", 0L, 1L));
        when(weightConversionService.isWeighable("KG")).thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class, () -> createExchangeUseCase.execute(request));
        assertTrue(ex.getMessage().contains("Cantidad decimal inválida para producto pesable"));
    }

    @Test
    @DisplayName("Should throw BusinessException when returned non-weighable product quantity is invalid integer")
    void shouldThrowExceptionWhenReturnedNonWeighableQtyInvalidInteger() {
        CreateExchangeRequest request = new CreateExchangeRequest(
                saleUuid, "DEFECTO", "Notas",
                List.of(new ReturnItemRequest(10L, "1.5")),
                List.of(new ExchangeItemRequest(12L, "1")),
                1L, BigDecimal.ZERO, false
        );

        setupUserContext("cashier1", "ROLE_CJTURNO", outletId);
        setupCommonMasterData();
        SaleItemDomain item = new SaleItemDomain(1L, 10L, 2, "2 UN", new BigDecimal("5000"), new BigDecimal("10000"), 1L);
        SaleDomain originalSale = createOriginalSale(100L, outletId, clientId, item);
        when(saleRepositoryPort.findByCode(saleUuid)).thenReturn(Optional.of(originalSale));
        when(personRepositoryPort.findById(clientId)).thenReturn(Optional.of(createClient(clientId)));

        ProductDomain product = createProduct(10L, "Jabon", 10, new BigDecimal("5000"), outletId, 1L);
        when(productOutletRepositoryPort.findById(10L)).thenReturn(Optional.of(product));
        when(masterTree.getById(1L)).thenReturn(new MasterRoot(1L, "UND", "Unidad", 0L, 1L));
        when(weightConversionService.isWeighable("UND")).thenReturn(false);

        BusinessException ex = assertThrows(BusinessException.class, () -> createExchangeUseCase.execute(request));
        assertTrue(ex.getMessage().contains("La cantidad debe ser un entero para"));
    }

    @Test
    @DisplayName("Should throw BusinessException when returned quantity is zero or negative")
    void shouldThrowExceptionWhenReturnedQtyZeroOrNegative() {
        CreateExchangeRequest request = new CreateExchangeRequest(
                saleUuid, "DEFECTO", "Notas",
                List.of(new ReturnItemRequest(10L, "0")),
                List.of(new ExchangeItemRequest(12L, "1")),
                1L, BigDecimal.ZERO, false
        );

        setupUserContext("cashier1", "ROLE_CJTURNO", outletId);
        setupCommonMasterData();
        SaleItemDomain item = new SaleItemDomain(1L, 10L, 2, "2 UN", new BigDecimal("5000"), new BigDecimal("10000"), 1L);
        SaleDomain originalSale = createOriginalSale(100L, outletId, clientId, item);
        when(saleRepositoryPort.findByCode(saleUuid)).thenReturn(Optional.of(originalSale));
        when(personRepositoryPort.findById(clientId)).thenReturn(Optional.of(createClient(clientId)));

        ProductDomain product = createProduct(10L, "Jabon", 10, new BigDecimal("5000"), outletId, 1L);
        when(productOutletRepositoryPort.findById(10L)).thenReturn(Optional.of(product));
        when(masterTree.getById(1L)).thenReturn(new MasterRoot(1L, "UND", "Unidad", 0L, 1L));
        when(weightConversionService.isWeighable("UND")).thenReturn(false);

        BusinessException ex = assertThrows(BusinessException.class, () -> createExchangeUseCase.execute(request));
        assertTrue(ex.getMessage().contains("debe ser mayor a cero"));
    }

    @Test
    @DisplayName("Should throw BusinessException when returned quantity exceeds original sold quantity")
    void shouldThrowExceptionWhenReturnedQtyExceedsOriginal() {
        CreateExchangeRequest request = new CreateExchangeRequest(
                saleUuid, "DEFECTO", "Notas",
                List.of(new ReturnItemRequest(10L, "5")),
                List.of(new ExchangeItemRequest(12L, "1")),
                1L, BigDecimal.ZERO, false
        );

        setupUserContext("cashier1", "ROLE_CJTURNO", outletId);
        setupCommonMasterData();
        SaleItemDomain item = new SaleItemDomain(1L, 10L, 2, "2 UN", new BigDecimal("5000"), new BigDecimal("10000"), 1L);
        SaleDomain originalSale = createOriginalSale(100L, outletId, clientId, item);
        when(saleRepositoryPort.findByCode(saleUuid)).thenReturn(Optional.of(originalSale));
        when(personRepositoryPort.findById(clientId)).thenReturn(Optional.of(createClient(clientId)));

        ProductDomain product = createProduct(10L, "Jabon", 10, new BigDecimal("5000"), outletId, 1L);
        when(productOutletRepositoryPort.findById(10L)).thenReturn(Optional.of(product));
        when(masterTree.getById(1L)).thenReturn(new MasterRoot(1L, "UND", "Unidad", 0L, 1L));
        when(weightConversionService.isWeighable("UND")).thenReturn(false);

        BusinessException ex = assertThrows(BusinessException.class, () -> createExchangeUseCase.execute(request));
        assertTrue(ex.getMessage().contains("supera la vendida originalmente"));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when replacement product is not found")
    void shouldThrowExceptionWhenReplacementProductNotFound() {
        CreateExchangeRequest request = new CreateExchangeRequest(
                saleUuid, "DEFECTO", "Notas",
                List.of(new ReturnItemRequest(10L, "1")),
                List.of(new ExchangeItemRequest(12L, "1")),
                1L, BigDecimal.ZERO, false
        );

        setupUserContext("cashier1", "ROLE_CJTURNO", outletId);
        setupCommonMasterData();
        SaleItemDomain item = new SaleItemDomain(1L, 10L, 2, "2 UN", new BigDecimal("5000"), new BigDecimal("10000"), 1L);
        SaleDomain originalSale = createOriginalSale(100L, outletId, clientId, item);
        when(saleRepositoryPort.findByCode(saleUuid)).thenReturn(Optional.of(originalSale));
        when(personRepositoryPort.findById(clientId)).thenReturn(Optional.of(createClient(clientId)));

        ProductDomain returnedProduct = createProduct(10L, "Jabon", 10, new BigDecimal("5000"), outletId, 1L);
        when(productOutletRepositoryPort.findById(10L)).thenReturn(Optional.of(returnedProduct));
        when(productOutletRepositoryPort.findById(12L)).thenReturn(Optional.empty());
        when(masterTree.getById(1L)).thenReturn(new MasterRoot(1L, "UND", "Unidad", 0L, 1L));
        when(weightConversionService.isWeighable("UND")).thenReturn(false);
        when(weightConversionService.formatFromBaseUnit(1, "UND")).thenReturn("1 UN");

        ReturnItemDomain returnItem = new ReturnItemDomain(1L, 10L, 1, "1 UN", new BigDecimal("5000"), new BigDecimal("5000"), 1L);
        ReturnDomain returnDomain = ReturnDomain.create(100L, "DEFECTO", "Notas", "CAMBIO", 50L, employeePersonId, outletId, clientId, List.of(returnItem));
        when(returnRepositoryPort.save(any(ReturnDomain.class))).thenReturn(returnDomain);

        assertThrows(ResourceNotFoundException.class, () -> createExchangeUseCase.execute(request));
    }

    @Test
    @DisplayName("Should throw BusinessException when replacement product belongs to another outlet")
    void shouldThrowExceptionWhenReplacementProductBelongsToAnotherOutlet() {
        CreateExchangeRequest request = new CreateExchangeRequest(
                saleUuid, "DEFECTO", "Notas",
                List.of(new ReturnItemRequest(10L, "1")),
                List.of(new ExchangeItemRequest(12L, "1")),
                1L, BigDecimal.ZERO, false
        );

        setupUserContext("cashier1", "ROLE_CJTURNO", outletId);
        setupCommonMasterData();
        SaleItemDomain item = new SaleItemDomain(1L, 10L, 2, "2 UN", new BigDecimal("5000"), new BigDecimal("10000"), 1L);
        SaleDomain originalSale = createOriginalSale(100L, outletId, clientId, item);
        when(saleRepositoryPort.findByCode(saleUuid)).thenReturn(Optional.of(originalSale));
        when(personRepositoryPort.findById(clientId)).thenReturn(Optional.of(createClient(clientId)));

        ProductDomain returnedProduct = createProduct(10L, "Jabon", 10, new BigDecimal("5000"), outletId, 1L);
        ProductDomain replacementProductOtherOutlet = createProduct(12L, "Shampoo", 15, new BigDecimal("7000"), 2L, 1L);

        when(productOutletRepositoryPort.findById(10L)).thenReturn(Optional.of(returnedProduct));
        when(productOutletRepositoryPort.findById(12L)).thenReturn(Optional.of(replacementProductOtherOutlet));
        when(masterTree.getById(1L)).thenReturn(new MasterRoot(1L, "UND", "Unidad", 0L, 1L));
        when(weightConversionService.isWeighable("UND")).thenReturn(false);
        when(weightConversionService.formatFromBaseUnit(1, "UND")).thenReturn("1 UN");

        ReturnItemDomain returnItem = new ReturnItemDomain(1L, 10L, 1, "1 UN", new BigDecimal("5000"), new BigDecimal("5000"), 1L);
        ReturnDomain returnDomain = ReturnDomain.create(100L, "DEFECTO", "Notas", "CAMBIO", 50L, employeePersonId, outletId, clientId, List.of(returnItem));
        when(returnRepositoryPort.save(any(ReturnDomain.class))).thenReturn(returnDomain);

        BusinessException ex = assertThrows(BusinessException.class, () -> createExchangeUseCase.execute(request));
        assertTrue(ex.getMessage().contains("pertenece a otra tienda"));
    }

    @Test
    @DisplayName("Should throw BusinessException when replacement weighable product quantity is invalid decimal")
    void shouldThrowExceptionWhenReplacementWeighableQtyInvalidDecimal() {
        CreateExchangeRequest request = new CreateExchangeRequest(
                saleUuid, "DEFECTO", "Notas",
                List.of(new ReturnItemRequest(10L, "1")),
                List.of(new ExchangeItemRequest(12L, "invalid")),
                1L, BigDecimal.ZERO, false
        );

        setupUserContext("cashier1", "ROLE_CJTURNO", outletId);
        setupCommonMasterData();
        SaleItemDomain item = new SaleItemDomain(1L, 10L, 2, "2 UN", new BigDecimal("5000"), new BigDecimal("10000"), 1L);
        SaleDomain originalSale = createOriginalSale(100L, outletId, clientId, item);
        when(saleRepositoryPort.findByCode(saleUuid)).thenReturn(Optional.of(originalSale));
        when(personRepositoryPort.findById(clientId)).thenReturn(Optional.of(createClient(clientId)));

        ProductDomain returnedProduct = createProduct(10L, "Jabon", 10, new BigDecimal("5000"), outletId, 1L);
        ProductDomain replacementProduct = createProduct(12L, "Carne", 15, new BigDecimal("7000"), outletId, 2L);

        when(productOutletRepositoryPort.findById(10L)).thenReturn(Optional.of(returnedProduct));
        when(productOutletRepositoryPort.findById(12L)).thenReturn(Optional.of(replacementProduct));
        when(masterTree.getById(1L)).thenReturn(new MasterRoot(1L, "UND", "Unidad", 0L, 1L));
        when(masterTree.getById(2L)).thenReturn(new MasterRoot(2L, "KG", "Kilogramo", 0L, 1L));
        when(weightConversionService.isWeighable("UND")).thenReturn(false);
        when(weightConversionService.isWeighable("KG")).thenReturn(true);
        when(weightConversionService.formatFromBaseUnit(1, "UND")).thenReturn("1 UN");

        ReturnItemDomain returnItem = new ReturnItemDomain(1L, 10L, 1, "1 UN", new BigDecimal("5000"), new BigDecimal("5000"), 1L);
        ReturnDomain returnDomain = ReturnDomain.create(100L, "DEFECTO", "Notas", "CAMBIO", 50L, employeePersonId, outletId, clientId, List.of(returnItem));
        when(returnRepositoryPort.save(any(ReturnDomain.class))).thenReturn(returnDomain);

        BusinessException ex = assertThrows(BusinessException.class, () -> createExchangeUseCase.execute(request));
        assertTrue(ex.getMessage().contains("Cantidad inválida para producto pesable"));
    }

    @Test
    @DisplayName("Should throw BusinessException when replacement non-weighable product quantity is invalid integer")
    void shouldThrowExceptionWhenReplacementNonWeighableQtyInvalidInteger() {
        CreateExchangeRequest request = new CreateExchangeRequest(
                saleUuid, "DEFECTO", "Notas",
                List.of(new ReturnItemRequest(10L, "1")),
                List.of(new ExchangeItemRequest(12L, "2.5")),
                1L, BigDecimal.ZERO, false
        );

        setupUserContext("cashier1", "ROLE_CJTURNO", outletId);
        setupCommonMasterData();
        SaleItemDomain item = new SaleItemDomain(1L, 10L, 2, "2 UN", new BigDecimal("5000"), new BigDecimal("10000"), 1L);
        SaleDomain originalSale = createOriginalSale(100L, outletId, clientId, item);
        when(saleRepositoryPort.findByCode(saleUuid)).thenReturn(Optional.of(originalSale));
        when(personRepositoryPort.findById(clientId)).thenReturn(Optional.of(createClient(clientId)));

        ProductDomain returnedProduct = createProduct(10L, "Jabon", 10, new BigDecimal("5000"), outletId, 1L);
        ProductDomain replacementProduct = createProduct(12L, "Shampoo", 15, new BigDecimal("7000"), outletId, 1L);

        when(productOutletRepositoryPort.findById(10L)).thenReturn(Optional.of(returnedProduct));
        when(productOutletRepositoryPort.findById(12L)).thenReturn(Optional.of(replacementProduct));
        when(masterTree.getById(1L)).thenReturn(new MasterRoot(1L, "UND", "Unidad", 0L, 1L));
        when(weightConversionService.isWeighable("UND")).thenReturn(false);
        when(weightConversionService.formatFromBaseUnit(1, "UND")).thenReturn("1 UN");

        ReturnItemDomain returnItem = new ReturnItemDomain(1L, 10L, 1, "1 UN", new BigDecimal("5000"), new BigDecimal("5000"), 1L);
        ReturnDomain returnDomain = ReturnDomain.create(100L, "DEFECTO", "Notas", "CAMBIO", 50L, employeePersonId, outletId, clientId, List.of(returnItem));
        when(returnRepositoryPort.save(any(ReturnDomain.class))).thenReturn(returnDomain);

        BusinessException ex = assertThrows(BusinessException.class, () -> createExchangeUseCase.execute(request));
        assertTrue(ex.getMessage().contains("La cantidad debe ser entero para"));
    }

    @Test
    @DisplayName("Should throw BusinessException when replacement product quantity is zero or negative")
    void shouldThrowExceptionWhenReplacementQtyZeroOrNegative() {
        CreateExchangeRequest request = new CreateExchangeRequest(
                saleUuid, "DEFECTO", "Notas",
                List.of(new ReturnItemRequest(10L, "1")),
                List.of(new ExchangeItemRequest(12L, "0")),
                1L, BigDecimal.ZERO, false
        );

        setupUserContext("cashier1", "ROLE_CJTURNO", outletId);
        setupCommonMasterData();
        SaleItemDomain item = new SaleItemDomain(1L, 10L, 2, "2 UN", new BigDecimal("5000"), new BigDecimal("10000"), 1L);
        SaleDomain originalSale = createOriginalSale(100L, outletId, clientId, item);
        when(saleRepositoryPort.findByCode(saleUuid)).thenReturn(Optional.of(originalSale));
        when(personRepositoryPort.findById(clientId)).thenReturn(Optional.of(createClient(clientId)));

        ProductDomain returnedProduct = createProduct(10L, "Jabon", 10, new BigDecimal("5000"), outletId, 1L);
        ProductDomain replacementProduct = createProduct(12L, "Shampoo", 15, new BigDecimal("7000"), outletId, 1L);

        when(productOutletRepositoryPort.findById(10L)).thenReturn(Optional.of(returnedProduct));
        when(productOutletRepositoryPort.findById(12L)).thenReturn(Optional.of(replacementProduct));
        when(masterTree.getById(1L)).thenReturn(new MasterRoot(1L, "UND", "Unidad", 0L, 1L));
        when(weightConversionService.isWeighable("UND")).thenReturn(false);
        when(weightConversionService.formatFromBaseUnit(1, "UND")).thenReturn("1 UN");

        ReturnItemDomain returnItem = new ReturnItemDomain(1L, 10L, 1, "1 UN", new BigDecimal("5000"), new BigDecimal("5000"), 1L);
        ReturnDomain returnDomain = ReturnDomain.create(100L, "DEFECTO", "Notas", "CAMBIO", 50L, employeePersonId, outletId, clientId, List.of(returnItem));
        when(returnRepositoryPort.save(any(ReturnDomain.class))).thenReturn(returnDomain);

        BusinessException ex = assertThrows(BusinessException.class, () -> createExchangeUseCase.execute(request));
        assertTrue(ex.getMessage().contains("debe ser mayor a cero"));
    }

    // ==========================================
    // SUCCESSFUL EXCHANGE SCENARIOS
    // ==========================================

    @Test
    @DisplayName("Should process product exchange successfully with cash surplus calculation")
    void shouldProcessExchangeWithCashSurplusSuccessfully() {
        CreateExchangeRequest request = new CreateExchangeRequest(
                saleUuid, "DEFECTO", "Notas de intercambio",
                List.of(new ReturnItemRequest(10L, "1")),
                List.of(new ExchangeItemRequest(12L, "2")),
                1L, new BigDecimal("20000.00"), true
        );

        setupUserContext("cashier1", "ROLE_CJTURNO", outletId);
        setupCommonMasterData();

        SaleItemDomain originalItem = new SaleItemDomain(1L, 10L, 2, "2 UN", new BigDecimal("5000.00"), new BigDecimal("10000.00"), 1L);
        SaleDomain originalSale = createOriginalSale(100L, outletId, clientId, originalItem);
        when(saleRepositoryPort.findByCode(saleUuid)).thenReturn(Optional.of(originalSale));
        when(personRepositoryPort.findById(clientId)).thenReturn(Optional.of(createClient(clientId)));

        ProductDomain returnedProduct = createProduct(10L, "Jabon 100g", 10, new BigDecimal("5000.00"), outletId, 1L);
        ProductDomain replacementProduct = createProduct(12L, "Shampoo 250ml", 15, new BigDecimal("7000.00"), outletId, 1L);

        when(productOutletRepositoryPort.findById(10L)).thenReturn(Optional.of(returnedProduct));
        when(productOutletRepositoryPort.findById(12L)).thenReturn(Optional.of(replacementProduct));

        when(masterTree.getById(1L)).thenReturn(new MasterRoot(1L, "UND", "Unidad", 0L, 1L));
        when(weightConversionService.isWeighable("UND")).thenReturn(false);
        when(weightConversionService.formatFromBaseUnit(1, "UND")).thenReturn("1 UN");
        when(weightConversionService.formatFromBaseUnit(2, "UND")).thenReturn("2 UN");

        ReturnItemDomain returnItem = new ReturnItemDomain(1L, 10L, 1, "1 UN", new BigDecimal("5000.00"), new BigDecimal("5000.00"), 1L);
        ReturnDomain returnDomain = ReturnDomain.create(100L, "DEFECTO", "Notas de intercambio", "CAMBIO", 50L, employeePersonId, outletId, clientId, List.of(returnItem));
        when(returnRepositoryPort.save(any(ReturnDomain.class))).thenReturn(returnDomain);

        SaleItemDomain newSaleItem = new SaleItemDomain(2L, 12L, 2, "2 UN", new BigDecimal("7000.00"), new BigDecimal("14000.00"), 1L);
        SaleDomain newSale = SaleDomain.fromPersistence(
                101L, UUID.randomUUID(), new BigDecimal("14000.00"), new BigDecimal("20000.00"), new BigDecimal("6000.00"),
                1L, 1L, clientId, outletId, employeePersonId, LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), List.of(newSaleItem)
        );
        when(saleRepositoryPort.save(any(SaleDomain.class))).thenReturn(newSale);

        ExchangeResponse response = createExchangeUseCase.execute(request);

        assertNotNull(response);
        assertEquals(saleUuid, response.returnDetail().originalSaleCode());
        assertEquals("CAMBIO", response.returnDetail().resolutionType());
        assertEquals(new BigDecimal("5000.00"), response.totalReturned());
        assertEquals(new BigDecimal("14000.00"), response.totalNewItems());
        assertEquals(new BigDecimal("9000.00"), response.netDifference());
        assertTrue(response.paymentStatusMessage().contains("Excedente de $9000.00 cobrado exitosamente"));

        // Stock updates: returned stock increased from 10 to 11, replacement decreased from 15 to 13
        assertEquals(11, returnedProduct.getStock());
        assertEquals(13, replacementProduct.getStock());

        verify(productOutletRepositoryPort, times(2)).save(any(ProductDomain.class));
        verify(returnRepositoryPort, times(1)).save(any(ReturnDomain.class));
        verify(saleRepositoryPort, times(1)).save(any(SaleDomain.class));
    }

    @Test
    @DisplayName("Should process cash surplus exchange using default amountReceived when null in request")
    void shouldProcessCashSurplus_DefaultAmountReceivedWhenNull() {
        CreateExchangeRequest request = new CreateExchangeRequest(
                saleUuid, "DEFECTO", "Notas",
                List.of(new ReturnItemRequest(10L, "1")),
                List.of(new ExchangeItemRequest(12L, "2")),
                1L, null, false
        );

        setupUserContext("cashier1", "ROLE_CJTURNO", outletId);
        setupCommonMasterData();

        SaleItemDomain originalItem = new SaleItemDomain(1L, 10L, 2, "2 UN", new BigDecimal("5000.00"), new BigDecimal("10000.00"), 1L);
        SaleDomain originalSale = createOriginalSale(100L, outletId, clientId, originalItem);
        when(saleRepositoryPort.findByCode(saleUuid)).thenReturn(Optional.of(originalSale));
        when(personRepositoryPort.findById(clientId)).thenReturn(Optional.of(createClient(clientId)));

        ProductDomain returnedProduct = createProduct(10L, "Jabon", 10, new BigDecimal("5000.00"), outletId, 1L);
        ProductDomain replacementProduct = createProduct(12L, "Shampoo", 15, new BigDecimal("7000.00"), outletId, 1L);

        when(productOutletRepositoryPort.findById(10L)).thenReturn(Optional.of(returnedProduct));
        when(productOutletRepositoryPort.findById(12L)).thenReturn(Optional.of(replacementProduct));

        when(masterTree.getById(1L)).thenReturn(new MasterRoot(1L, "UND", "Unidad", 0L, 1L));
        when(weightConversionService.isWeighable("UND")).thenReturn(false);
        when(weightConversionService.formatFromBaseUnit(1, "UND")).thenReturn("1 UN");
        when(weightConversionService.formatFromBaseUnit(2, "UND")).thenReturn("2 UN");

        ReturnItemDomain returnItem = new ReturnItemDomain(1L, 10L, 1, "1 UN", new BigDecimal("5000.00"), new BigDecimal("5000.00"), 1L);
        ReturnDomain returnDomain = ReturnDomain.create(100L, "DEFECTO", "Notas", "CAMBIO", 50L, employeePersonId, outletId, clientId, List.of(returnItem));
        when(returnRepositoryPort.save(any(ReturnDomain.class))).thenReturn(returnDomain);

        SaleItemDomain newSaleItem = new SaleItemDomain(2L, 12L, 2, "2 UN", new BigDecimal("7000.00"), new BigDecimal("14000.00"), 1L);
        SaleDomain newSale = SaleDomain.fromPersistence(
                101L, UUID.randomUUID(), new BigDecimal("14000.00"), new BigDecimal("9000.00"), BigDecimal.ZERO,
                1L, 1L, clientId, outletId, employeePersonId, LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), List.of(newSaleItem)
        );
        when(saleRepositoryPort.save(any(SaleDomain.class))).thenReturn(newSale);

        ExchangeResponse response = createExchangeUseCase.execute(request);
        assertNotNull(response);
        assertEquals(new BigDecimal("9000.00"), response.netDifference());
        assertTrue(response.paymentStatusMessage().contains("cobrado exitosamente"));
    }

    @Test
    @DisplayName("Should process fiado surplus exchange with existing credit account")
    void shouldProcessFiadoSurplus_ExistingCreditAccount() {
        CreateExchangeRequest request = new CreateExchangeRequest(
                saleUuid, "DEFECTO", "Notas",
                List.of(new ReturnItemRequest(10L, "1")),
                List.of(new ExchangeItemRequest(12L, "2")),
                2L, BigDecimal.ZERO, false // 2L = FIA
        );

        setupUserContext("cashier1", "ROLE_CJTURNO", outletId);
        setupCommonMasterData();

        SaleItemDomain originalItem = new SaleItemDomain(1L, 10L, 2, "2 UN", new BigDecimal("5000.00"), new BigDecimal("10000.00"), 1L);
        SaleDomain originalSale = createOriginalSale(100L, outletId, clientId, originalItem);
        when(saleRepositoryPort.findByCode(saleUuid)).thenReturn(Optional.of(originalSale));
        when(personRepositoryPort.findById(clientId)).thenReturn(Optional.of(createClient(clientId)));

        ProductDomain returnedProduct = createProduct(10L, "Jabon", 10, new BigDecimal("5000.00"), outletId, 1L);
        ProductDomain replacementProduct = createProduct(12L, "Shampoo", 15, new BigDecimal("7000.00"), outletId, 1L);

        when(productOutletRepositoryPort.findById(10L)).thenReturn(Optional.of(returnedProduct));
        when(productOutletRepositoryPort.findById(12L)).thenReturn(Optional.of(replacementProduct));

        when(masterTree.getById(1L)).thenReturn(new MasterRoot(1L, "UND", "Unidad", 0L, 1L));
        when(masterTree.getById(2L)).thenReturn(new MasterRoot(2L, "FIA", "Fiado", 0L, 1L));
        when(weightConversionService.isWeighable("UND")).thenReturn(false);
        when(weightConversionService.formatFromBaseUnit(1, "UND")).thenReturn("1 UN");
        when(weightConversionService.formatFromBaseUnit(2, "UND")).thenReturn("2 UN");

        ReturnItemDomain returnItem = new ReturnItemDomain(1L, 10L, 1, "1 UN", new BigDecimal("5000.00"), new BigDecimal("5000.00"), 1L);
        ReturnDomain returnDomain = ReturnDomain.create(100L, "DEFECTO", "Notas", "CAMBIO", 50L, employeePersonId, outletId, clientId, List.of(returnItem));
        when(returnRepositoryPort.save(any(ReturnDomain.class))).thenReturn(returnDomain);

        CreditAccountDomain existingCreditAccount = CreditAccountDomain.reconstruct(
                10L, clientId, outletId, new BigDecimal("150000.00"), new BigDecimal("10000.00"), 1L, LocalDateTime.now(), LocalDateTime.now()
        );
        when(creditRepositoryPort.findByClientIdAndOutletId(clientId, outletId)).thenReturn(Optional.of(existingCreditAccount));
        when(creditRepositoryPort.save((CreditAccountDomain) any())).thenReturn(existingCreditAccount);

        SaleItemDomain newSaleItem = new SaleItemDomain(2L, 12L, 2, "2 UN", new BigDecimal("7000.00"), new BigDecimal("14000.00"), 1L);
        SaleDomain newSale = SaleDomain.fromPersistence(
                101L, UUID.randomUUID(), new BigDecimal("14000.00"), BigDecimal.ZERO, BigDecimal.ZERO,
                2L, 1L, clientId, outletId, employeePersonId, LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), List.of(newSaleItem)
        );
        when(saleRepositoryPort.save(any(SaleDomain.class))).thenReturn(newSale);

        ExchangeResponse response = createExchangeUseCase.execute(request);

        assertNotNull(response);
        assertEquals(new BigDecimal("9000.00"), response.netDifference());
        assertTrue(response.paymentStatusMessage().contains("cargado a la libreta de fiado"));
        assertEquals(new BigDecimal("19000.00"), existingCreditAccount.getCurrentDebt());

        verify(creditRepositoryPort, times(1)).save((CreditAccountDomain) any(CreditAccountDomain.class));
        verify(creditRepositoryPort, times(1)).save((CreditTransactionDomain) any(CreditTransactionDomain.class));
    }

    @Test
    @DisplayName("Should process fiado surplus exchange and create new credit account when not found")
    void shouldProcessFiadoSurplus_CreatesNewCreditAccountWhenNotFound() {
        CreateExchangeRequest request = new CreateExchangeRequest(
                saleUuid, "DEFECTO", "Notas",
                List.of(new ReturnItemRequest(10L, "1")),
                List.of(new ExchangeItemRequest(12L, "2")),
                2L, BigDecimal.ZERO, false // 2L = FIA
        );

        setupUserContext("cashier1", "ROLE_CJTURNO", outletId);
        setupCommonMasterData();

        SaleItemDomain originalItem = new SaleItemDomain(1L, 10L, 2, "2 UN", new BigDecimal("5000.00"), new BigDecimal("10000.00"), 1L);
        SaleDomain originalSale = createOriginalSale(100L, outletId, clientId, originalItem);
        when(saleRepositoryPort.findByCode(saleUuid)).thenReturn(Optional.of(originalSale));
        when(personRepositoryPort.findById(clientId)).thenReturn(Optional.of(createClient(clientId)));

        ProductDomain returnedProduct = createProduct(10L, "Jabon", 10, new BigDecimal("5000.00"), outletId, 1L);
        ProductDomain replacementProduct = createProduct(12L, "Shampoo", 15, new BigDecimal("7000.00"), outletId, 1L);

        when(productOutletRepositoryPort.findById(10L)).thenReturn(Optional.of(returnedProduct));
        when(productOutletRepositoryPort.findById(12L)).thenReturn(Optional.of(replacementProduct));

        when(masterTree.getById(1L)).thenReturn(new MasterRoot(1L, "UND", "Unidad", 0L, 1L));
        when(masterTree.getById(2L)).thenReturn(new MasterRoot(2L, "FIA", "Fiado", 0L, 1L));
        when(weightConversionService.isWeighable("UND")).thenReturn(false);
        when(weightConversionService.formatFromBaseUnit(1, "UND")).thenReturn("1 UN");
        when(weightConversionService.formatFromBaseUnit(2, "UND")).thenReturn("2 UN");

        ReturnItemDomain returnItem = new ReturnItemDomain(1L, 10L, 1, "1 UN", new BigDecimal("5000.00"), new BigDecimal("5000.00"), 1L);
        ReturnDomain returnDomain = ReturnDomain.create(100L, "DEFECTO", "Notas", "CAMBIO", 50L, employeePersonId, outletId, clientId, List.of(returnItem));
        when(returnRepositoryPort.save(any(ReturnDomain.class))).thenReturn(returnDomain);

        when(creditRepositoryPort.findByClientIdAndOutletId(clientId, outletId)).thenReturn(Optional.empty());
        when(creditRepositoryPort.save((CreditAccountDomain) any())).thenAnswer(invocation -> invocation.getArgument(0));

        SaleItemDomain newSaleItem = new SaleItemDomain(2L, 12L, 2, "2 UN", new BigDecimal("7000.00"), new BigDecimal("14000.00"), 1L);
        SaleDomain newSale = SaleDomain.fromPersistence(
                101L, UUID.randomUUID(), new BigDecimal("14000.00"), BigDecimal.ZERO, BigDecimal.ZERO,
                2L, 1L, clientId, outletId, employeePersonId, LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), List.of(newSaleItem)
        );
        when(saleRepositoryPort.save(any(SaleDomain.class))).thenReturn(newSale);

        ExchangeResponse response = createExchangeUseCase.execute(request);

        assertNotNull(response);
        assertEquals(new BigDecimal("9000.00"), response.netDifference());
        assertTrue(response.paymentStatusMessage().contains("cargado a la libreta de fiado"));

        verify(creditRepositoryPort, times(1)).save((CreditAccountDomain) any());
        verify(creditRepositoryPort, times(1)).save((CreditTransactionDomain) any());
    }

    @Test
    @DisplayName("Should process equal value replacement exchange successfully")
    void shouldProcessEqualValueReplacementSuccessfully() {
        CreateExchangeRequest request = new CreateExchangeRequest(
                saleUuid, "DEFECTO", "Notas",
                List.of(new ReturnItemRequest(10L, "2")),
                List.of(new ExchangeItemRequest(12L, "1")),
                1L, BigDecimal.ZERO, false
        );

        setupUserContext("cashier1", "ROLE_CJTURNO", outletId);
        setupCommonMasterData();

        SaleItemDomain originalItem = new SaleItemDomain(1L, 10L, 2, "2 UN", new BigDecimal("5000.00"), new BigDecimal("10000.00"), 1L);
        SaleDomain originalSale = createOriginalSale(100L, outletId, clientId, originalItem);
        when(saleRepositoryPort.findByCode(saleUuid)).thenReturn(Optional.of(originalSale));
        when(personRepositoryPort.findById(clientId)).thenReturn(Optional.of(createClient(clientId)));

        ProductDomain returnedProduct = createProduct(10L, "Jabon", 10, new BigDecimal("5000.00"), outletId, 1L);
        ProductDomain replacementProduct = createProduct(12L, "Perfume", 5, new BigDecimal("10000.00"), outletId, 1L);

        when(productOutletRepositoryPort.findById(10L)).thenReturn(Optional.of(returnedProduct));
        when(productOutletRepositoryPort.findById(12L)).thenReturn(Optional.of(replacementProduct));

        when(masterTree.getById(1L)).thenReturn(new MasterRoot(1L, "UND", "Unidad", 0L, 1L));
        when(weightConversionService.isWeighable("UND")).thenReturn(false);
        when(weightConversionService.formatFromBaseUnit(2, "UND")).thenReturn("2 UN");
        when(weightConversionService.formatFromBaseUnit(1, "UND")).thenReturn("1 UN");

        ReturnItemDomain returnItem = new ReturnItemDomain(1L, 10L, 2, "2 UN", new BigDecimal("5000.00"), new BigDecimal("10000.00"), 1L);
        ReturnDomain returnDomain = ReturnDomain.create(100L, "DEFECTO", "Notas", "CAMBIO", 50L, employeePersonId, outletId, clientId, List.of(returnItem));
        when(returnRepositoryPort.save(any(ReturnDomain.class))).thenReturn(returnDomain);

        SaleItemDomain newSaleItem = new SaleItemDomain(2L, 12L, 1, "1 UN", new BigDecimal("10000.00"), new BigDecimal("10000.00"), 1L);
        SaleDomain newSale = SaleDomain.fromPersistence(
                101L, UUID.randomUUID(), new BigDecimal("10000.00"), BigDecimal.ZERO, BigDecimal.ZERO,
                1L, 1L, clientId, outletId, employeePersonId, LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), List.of(newSaleItem)
        );
        when(saleRepositoryPort.save(any(SaleDomain.class))).thenReturn(newSale);

        ExchangeResponse response = createExchangeUseCase.execute(request);

        assertNotNull(response);
        assertEquals(BigDecimal.ZERO.setScale(2), response.netDifference());
        assertEquals("Cambio realizado de igual valor. Sin saldos pendientes.", response.paymentStatusMessage());
    }

    @Test
    @DisplayName("Should process exchange with client surplus and apply to reduce existing active debt")
    void shouldProcessSurplusForClient_AndReduceExistingDebt() {
        CreateExchangeRequest request = new CreateExchangeRequest(
                saleUuid, "DEFECTO", "Notas",
                List.of(new ReturnItemRequest(10L, "2")), // Returns 2 x $5000 = $10,000
                List.of(new ExchangeItemRequest(12L, "1")), // Replaces with 1 x $6000 = $6,000 -> Surplus = $4,000
                1L, BigDecimal.ZERO, false
        );

        setupUserContext("cashier1", "ROLE_CJTURNO", outletId);
        setupCommonMasterData();

        SaleItemDomain originalItem = new SaleItemDomain(1L, 10L, 2, "2 UN", new BigDecimal("5000.00"), new BigDecimal("10000.00"), 1L);
        SaleDomain originalSale = createOriginalSale(100L, outletId, clientId, originalItem);
        when(saleRepositoryPort.findByCode(saleUuid)).thenReturn(Optional.of(originalSale));
        when(personRepositoryPort.findById(clientId)).thenReturn(Optional.of(createClient(clientId)));

        ProductDomain returnedProduct = createProduct(10L, "Jabon", 10, new BigDecimal("5000.00"), outletId, 1L);
        ProductDomain replacementProduct = createProduct(12L, "Crema", 5, new BigDecimal("6000.00"), outletId, 1L);

        when(productOutletRepositoryPort.findById(10L)).thenReturn(Optional.of(returnedProduct));
        when(productOutletRepositoryPort.findById(12L)).thenReturn(Optional.of(replacementProduct));

        when(masterTree.getById(1L)).thenReturn(new MasterRoot(1L, "UND", "Unidad", 0L, 1L));
        when(weightConversionService.isWeighable("UND")).thenReturn(false);
        when(weightConversionService.formatFromBaseUnit(2, "UND")).thenReturn("2 UN");
        when(weightConversionService.formatFromBaseUnit(1, "UND")).thenReturn("1 UN");

        ReturnItemDomain returnItem = new ReturnItemDomain(1L, 10L, 2, "2 UN", new BigDecimal("5000.00"), new BigDecimal("10000.00"), 1L);
        ReturnDomain returnDomain = ReturnDomain.create(100L, "DEFECTO", "Notas", "CAMBIO", 50L, employeePersonId, outletId, clientId, List.of(returnItem));
        when(returnRepositoryPort.save(any(ReturnDomain.class))).thenReturn(returnDomain);

        CreditAccountDomain creditAccount = CreditAccountDomain.reconstruct(
                10L, clientId, outletId, new BigDecimal("150000.00"), new BigDecimal("15000.00"), 1L, LocalDateTime.now(), LocalDateTime.now()
        );
        when(creditRepositoryPort.findByClientIdAndOutletId(clientId, outletId)).thenReturn(Optional.of(creditAccount));
        when(creditRepositoryPort.save((CreditAccountDomain) any(CreditAccountDomain.class))).thenReturn(creditAccount);

        SaleItemDomain newSaleItem = new SaleItemDomain(2L, 12L, 1, "1 UN", new BigDecimal("6000.00"), new BigDecimal("6000.00"), 1L);
        SaleDomain newSale = SaleDomain.fromPersistence(
                101L, UUID.randomUUID(), new BigDecimal("6000.00"), BigDecimal.ZERO, BigDecimal.ZERO,
                1L, 1L, clientId, outletId, employeePersonId, LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), List.of(newSaleItem)
        );
        when(saleRepositoryPort.save(any(SaleDomain.class))).thenReturn(newSale);

        ExchangeResponse response = createExchangeUseCase.execute(request);

        assertNotNull(response);
        assertEquals(new BigDecimal("-4000.00"), response.netDifference());
        assertTrue(response.paymentStatusMessage().contains("aplicada para abonar a la deuda del cliente"));
        assertEquals(new BigDecimal("11000.00"), creditAccount.getCurrentDebt()); // Debt reduced from 15,000 to 11,000

        verify(creditRepositoryPort, times(1)).save((CreditAccountDomain) any(CreditAccountDomain.class));
        verify(creditRepositoryPort, times(1)).save((CreditTransactionDomain) any(CreditTransactionDomain.class));
    }

    @Test
    @DisplayName("Should process exchange with client surplus and return cash to client when no debt exists")
    void shouldProcessSurplusForClient_NoActiveDebtOrNoAccount() {
        CreateExchangeRequest request = new CreateExchangeRequest(
                saleUuid, "DEFECTO", "Notas",
                List.of(new ReturnItemRequest(10L, "2")), // Returns $10,000
                List.of(new ExchangeItemRequest(12L, "1")), // Replaces $6,000 -> Surplus = $4,000
                1L, BigDecimal.ZERO, false
        );

        setupUserContext("cashier1", "ROLE_CJTURNO", outletId);
        setupCommonMasterData();

        SaleItemDomain originalItem = new SaleItemDomain(1L, 10L, 2, "2 UN", new BigDecimal("5000.00"), new BigDecimal("10000.00"), 1L);
        SaleDomain originalSale = createOriginalSale(100L, outletId, clientId, originalItem);
        when(saleRepositoryPort.findByCode(saleUuid)).thenReturn(Optional.of(originalSale));
        when(personRepositoryPort.findById(clientId)).thenReturn(Optional.of(createClient(clientId)));

        ProductDomain returnedProduct = createProduct(10L, "Jabon", 10, new BigDecimal("5000.00"), outletId, 1L);
        ProductDomain replacementProduct = createProduct(12L, "Crema", 5, new BigDecimal("6000.00"), outletId, 1L);

        when(productOutletRepositoryPort.findById(10L)).thenReturn(Optional.of(returnedProduct));
        when(productOutletRepositoryPort.findById(12L)).thenReturn(Optional.of(replacementProduct));

        when(masterTree.getById(1L)).thenReturn(new MasterRoot(1L, "UND", "Unidad", 0L, 1L));
        when(weightConversionService.isWeighable("UND")).thenReturn(false);
        when(weightConversionService.formatFromBaseUnit(2, "UND")).thenReturn("2 UN");
        when(weightConversionService.formatFromBaseUnit(1, "UND")).thenReturn("1 UN");

        ReturnItemDomain returnItem = new ReturnItemDomain(1L, 10L, 2, "2 UN", new BigDecimal("5000.00"), new BigDecimal("10000.00"), 1L);
        ReturnDomain returnDomain = ReturnDomain.create(100L, "DEFECTO", "Notas", "CAMBIO", 50L, employeePersonId, outletId, clientId, List.of(returnItem));
        when(returnRepositoryPort.save(any(ReturnDomain.class))).thenReturn(returnDomain);

        when(creditRepositoryPort.findByClientIdAndOutletId(clientId, outletId)).thenReturn(Optional.empty());

        SaleItemDomain newSaleItem = new SaleItemDomain(2L, 12L, 1, "1 UN", new BigDecimal("6000.00"), new BigDecimal("6000.00"), 1L);
        SaleDomain newSale = SaleDomain.fromPersistence(
                101L, UUID.randomUUID(), new BigDecimal("6000.00"), BigDecimal.ZERO, BigDecimal.ZERO,
                1L, 1L, clientId, outletId, employeePersonId, LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), List.of(newSaleItem)
        );
        when(saleRepositoryPort.save(any(SaleDomain.class))).thenReturn(newSale);

        ExchangeResponse response = createExchangeUseCase.execute(request);

        assertNotNull(response);
        assertEquals(new BigDecimal("-4000.00"), response.netDifference());
        assertTrue(response.paymentStatusMessage().contains("entregada al cliente"));

        verify(creditRepositoryPort, never()).save((CreditAccountDomain) any(CreditAccountDomain.class));
    }

    @Test
    @DisplayName("Should allow exchange across different outlets when user is System Admin")
    void shouldAllowExchangeAcrossOutletsWhenUserIsAdmin() {
        CreateExchangeRequest request = new CreateExchangeRequest(
                saleUuid, "DEFECTO", "Notas",
                List.of(new ReturnItemRequest(10L, "1")),
                List.of(new ExchangeItemRequest(12L, "1")),
                1L, BigDecimal.ZERO, false
        );

        setupUserContext("adminUser", "ROLE_ADMIN", 1L);
        setupCommonMasterData();

        SaleItemDomain item = new SaleItemDomain(1L, 10L, 1, "1 UN", new BigDecimal("5000.00"), new BigDecimal("5000.00"), 1L);
        SaleDomain originalSale = createOriginalSale(100L, 2L, clientId, item); // Sale is in outlet 2L
        when(saleRepositoryPort.findByCode(saleUuid)).thenReturn(Optional.of(originalSale));
        when(personRepositoryPort.findById(clientId)).thenReturn(Optional.of(createClient(clientId)));

        ProductDomain returnedProduct = createProduct(10L, "Jabon", 10, new BigDecimal("5000.00"), 2L, 1L);
        ProductDomain replacementProduct = createProduct(12L, "Jabon Especial", 5, new BigDecimal("5000.00"), 2L, 1L);

        when(productOutletRepositoryPort.findById(10L)).thenReturn(Optional.of(returnedProduct));
        when(productOutletRepositoryPort.findById(12L)).thenReturn(Optional.of(replacementProduct));

        when(masterTree.getById(1L)).thenReturn(new MasterRoot(1L, "UND", "Unidad", 0L, 1L));
        when(weightConversionService.isWeighable("UND")).thenReturn(false);
        when(weightConversionService.formatFromBaseUnit(1, "UND")).thenReturn("1 UN");

        ReturnItemDomain returnItem = new ReturnItemDomain(1L, 10L, 1, "1 UN", new BigDecimal("5000.00"), new BigDecimal("5000.00"), 1L);
        ReturnDomain returnDomain = ReturnDomain.create(100L, "DEFECTO", "Notas", "CAMBIO", 50L, employeePersonId, 2L, clientId, List.of(returnItem));
        when(returnRepositoryPort.save(any(ReturnDomain.class))).thenReturn(returnDomain);

        SaleItemDomain newSaleItem = new SaleItemDomain(2L, 12L, 1, "1 UN", new BigDecimal("5000.00"), new BigDecimal("5000.00"), 1L);
        SaleDomain newSale = SaleDomain.fromPersistence(
                101L, UUID.randomUUID(), new BigDecimal("5000.00"), BigDecimal.ZERO, BigDecimal.ZERO,
                1L, 1L, clientId, 2L, employeePersonId, LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), List.of(newSaleItem)
        );
        when(saleRepositoryPort.save(any(SaleDomain.class))).thenReturn(newSale);

        ExchangeResponse response = createExchangeUseCase.execute(request);
        assertNotNull(response);
        assertEquals(2L, response.newSaleDetail().outletId());
    }

    // ==========================================
    // WEIGHABLE PRODUCTS UNIT FACTORS TESTS
    // ==========================================

    @Test
    @DisplayName("Should handle weighable conversion factor for KG and L unit codes")
    void shouldHandleWeighableFactors_KG_and_L() {
        CreateExchangeRequest request = new CreateExchangeRequest(
                saleUuid, "DEFECTO", "Notas",
                List.of(new ReturnItemRequest(10L, "1000")),
                List.of(new ExchangeItemRequest(12L, "1000")),
                1L, BigDecimal.ZERO, false
        );

        setupUserContext("cashier1", "ROLE_CJTURNO", outletId);
        setupCommonMasterData();

        SaleItemDomain originalItem = new SaleItemDomain(1L, 10L, 1000, "1000 KG", new BigDecimal("1000.00"), new BigDecimal("1000.00"), 2L);
        SaleDomain originalSale = createOriginalSale(100L, outletId, clientId, originalItem);
        when(saleRepositoryPort.findByCode(saleUuid)).thenReturn(Optional.of(originalSale));
        when(personRepositoryPort.findById(clientId)).thenReturn(Optional.of(createClient(clientId)));

        ProductDomain returnedProduct = createProduct(10L, "Carne", 5000, new BigDecimal("1000.00"), outletId, 2L);
        ProductDomain replacementProduct = createProduct(12L, "Leche", 5000, new BigDecimal("1000.00"), outletId, 2L);

        when(productOutletRepositoryPort.findById(10L)).thenReturn(Optional.of(returnedProduct));
        when(productOutletRepositoryPort.findById(12L)).thenReturn(Optional.of(replacementProduct));

        when(masterTree.getById(2L)).thenReturn(new MasterRoot(2L, "KG", "Kilogramo", 0L, 1L));
        when(weightConversionService.isWeighable("KG")).thenReturn(true);
        when(weightConversionService.convertToBaseUnit(any(BigDecimal.class), eq("KG"))).thenReturn(1000);
        when(weightConversionService.formatFromBaseUnit(1000, "KG")).thenReturn("1000 KG");

        ReturnItemDomain returnItem = new ReturnItemDomain(1L, 10L, 1000, "1000 KG", new BigDecimal("1000.00"), new BigDecimal("1000.00"), 2L);
        ReturnDomain returnDomain = ReturnDomain.create(100L, "DEFECTO", "Notas", "CAMBIO", 50L, employeePersonId, outletId, clientId, List.of(returnItem));
        when(returnRepositoryPort.save(any(ReturnDomain.class))).thenReturn(returnDomain);

        SaleItemDomain newSaleItem = new SaleItemDomain(2L, 12L, 1000, "1000 KG", new BigDecimal("1000.00"), new BigDecimal("1000.00"), 2L);
        SaleDomain newSale = SaleDomain.fromPersistence(
                101L, UUID.randomUUID(), new BigDecimal("1000.00"), BigDecimal.ZERO, BigDecimal.ZERO,
                1L, 1L, clientId, outletId, employeePersonId, LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), List.of(newSaleItem)
        );
        when(saleRepositoryPort.save(any(SaleDomain.class))).thenReturn(newSale);

        ExchangeResponse response = createExchangeUseCase.execute(request);
        assertNotNull(response);
        assertEquals(new BigDecimal("1000.00"), response.totalReturned());
        assertEquals(new BigDecimal("1000.00"), response.totalNewItems());
        assertEquals(BigDecimal.ZERO.setScale(2), response.netDifference());
    }

    @Test
    @DisplayName("Should handle weighable conversion factor for LB unit code")
    void shouldHandleWeighableFactors_LB() {
        CreateExchangeRequest request = new CreateExchangeRequest(
                saleUuid, "DEFECTO", "Notas",
                List.of(new ReturnItemRequest(10L, "454")),
                List.of(new ExchangeItemRequest(12L, "454")),
                1L, BigDecimal.ZERO, false
        );

        setupUserContext("cashier1", "ROLE_CJTURNO", outletId);
        setupCommonMasterData();

        SaleItemDomain originalItem = new SaleItemDomain(1L, 10L, 454, "454 LB", new BigDecimal("1000.00"), new BigDecimal("1000.90"), 3L);
        SaleDomain originalSale = createOriginalSale(100L, outletId, clientId, originalItem);
        when(saleRepositoryPort.findByCode(saleUuid)).thenReturn(Optional.of(originalSale));
        when(personRepositoryPort.findById(clientId)).thenReturn(Optional.of(createClient(clientId)));

        ProductDomain returnedProduct = createProduct(10L, "Queso LB", 5000, new BigDecimal("1000.00"), outletId, 3L);
        ProductDomain replacementProduct = createProduct(12L, "Pollo LB", 5000, new BigDecimal("1000.00"), outletId, 3L);

        when(productOutletRepositoryPort.findById(10L)).thenReturn(Optional.of(returnedProduct));
        when(productOutletRepositoryPort.findById(12L)).thenReturn(Optional.of(replacementProduct));

        when(masterTree.getById(3L)).thenReturn(new MasterRoot(3L, "LB", "Libra", 0L, 1L));
        when(weightConversionService.isWeighable("LB")).thenReturn(true);
        when(weightConversionService.convertToBaseUnit(any(BigDecimal.class), eq("LB"))).thenReturn(454);
        when(weightConversionService.formatFromBaseUnit(454, "LB")).thenReturn("454 LB");

        ReturnItemDomain returnItem = new ReturnItemDomain(1L, 10L, 454, "454 LB", new BigDecimal("1000.00"), new BigDecimal("1000.90"), 3L);
        ReturnDomain returnDomain = ReturnDomain.create(100L, "DEFECTO", "Notas", "CAMBIO", 50L, employeePersonId, outletId, clientId, List.of(returnItem));
        when(returnRepositoryPort.save(any(ReturnDomain.class))).thenReturn(returnDomain);

        SaleItemDomain newSaleItem = new SaleItemDomain(2L, 12L, 454, "454 LB", new BigDecimal("1000.00"), new BigDecimal("1000.90"), 3L);
        SaleDomain newSale = SaleDomain.fromPersistence(
                101L, UUID.randomUUID(), new BigDecimal("1000.90"), BigDecimal.ZERO, BigDecimal.ZERO,
                1L, 1L, clientId, outletId, employeePersonId, LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), List.of(newSaleItem)
        );
        when(saleRepositoryPort.save(any(SaleDomain.class))).thenReturn(newSale);

        ExchangeResponse response = createExchangeUseCase.execute(request);
        assertNotNull(response);
        assertEquals(new BigDecimal("1000.90"), response.totalReturned());
        assertEquals(new BigDecimal("1000.90"), response.totalNewItems());
        assertEquals(BigDecimal.ZERO.setScale(2), response.netDifference());
    }

    @Test
    @DisplayName("Should handle weighable conversion factor default case (e.g. GR)")
    void shouldHandleWeighableFactors_Default() {
        CreateExchangeRequest request = new CreateExchangeRequest(
                saleUuid, "DEFECTO", "Notas",
                List.of(new ReturnItemRequest(10L, "1000")),
                List.of(new ExchangeItemRequest(12L, "1000")),
                1L, BigDecimal.ZERO, false
        );

        setupUserContext("cashier1", "ROLE_CJTURNO", outletId);
        setupCommonMasterData();

        SaleItemDomain originalItem = new SaleItemDomain(1L, 10L, 1000, "1000 GR", new BigDecimal("5.00"), new BigDecimal("5000.00"), 4L);
        SaleDomain originalSale = createOriginalSale(100L, outletId, clientId, originalItem);
        when(saleRepositoryPort.findByCode(saleUuid)).thenReturn(Optional.of(originalSale));
        when(personRepositoryPort.findById(clientId)).thenReturn(Optional.of(createClient(clientId)));

        ProductDomain returnedProduct = createProduct(10L, "Dulce GR", 5000, new BigDecimal("5.00"), outletId, 4L);
        ProductDomain replacementProduct = createProduct(12L, "Fruta GR", 5000, new BigDecimal("5.00"), outletId, 4L);

        when(productOutletRepositoryPort.findById(10L)).thenReturn(Optional.of(returnedProduct));
        when(productOutletRepositoryPort.findById(12L)).thenReturn(Optional.of(replacementProduct));

        when(masterTree.getById(4L)).thenReturn(new MasterRoot(4L, "GR", "Gramo", 0L, 1L));
        when(weightConversionService.isWeighable("GR")).thenReturn(true);
        when(weightConversionService.convertToBaseUnit(any(BigDecimal.class), eq("GR"))).thenReturn(1000);
        when(weightConversionService.formatFromBaseUnit(1000, "GR")).thenReturn("1000 GR");

        ReturnItemDomain returnItem = new ReturnItemDomain(1L, 10L, 1000, "1000 GR", new BigDecimal("5.00"), new BigDecimal("5000.00"), 4L);
        ReturnDomain returnDomain = ReturnDomain.create(100L, "DEFECTO", "Notas", "CAMBIO", 50L, employeePersonId, outletId, clientId, List.of(returnItem));
        when(returnRepositoryPort.save(any(ReturnDomain.class))).thenReturn(returnDomain);

        SaleItemDomain newSaleItem = new SaleItemDomain(2L, 12L, 1000, "1000 GR", new BigDecimal("5.00"), new BigDecimal("5000.00"), 4L);
        SaleDomain newSale = SaleDomain.fromPersistence(
                101L, UUID.randomUUID(), new BigDecimal("5000.00"), BigDecimal.ZERO, BigDecimal.ZERO,
                1L, 1L, clientId, outletId, employeePersonId, LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), List.of(newSaleItem)
        );
        when(saleRepositoryPort.save(any(SaleDomain.class))).thenReturn(newSale);

        ExchangeResponse response = createExchangeUseCase.execute(request);
        assertNotNull(response);
        assertEquals(new BigDecimal("5000.00"), response.totalReturned());
        assertEquals(new BigDecimal("5000.00"), response.totalNewItems());
        assertEquals(BigDecimal.ZERO.setScale(2), response.netDifference());
    }

    @Test
    @DisplayName("Should default unitCode to UND when unitNode in MasterTree is null")
    void shouldDefaultUnitCodeToUND_WhenUnitNodeIsNull() {
        CreateExchangeRequest request = new CreateExchangeRequest(
                saleUuid, "DEFECTO", "Notas",
                List.of(new ReturnItemRequest(10L, "1")),
                List.of(new ExchangeItemRequest(12L, "1")),
                1L, BigDecimal.ZERO, false
        );

        setupUserContext("cashier1", "ROLE_CJTURNO", outletId);
        setupCommonMasterData();

        SaleItemDomain item = new SaleItemDomain(1L, 10L, 1, "1 UN", new BigDecimal("5000.00"), new BigDecimal("5000.00"), 999L);
        SaleDomain originalSale = createOriginalSale(100L, outletId, clientId, item);
        when(saleRepositoryPort.findByCode(saleUuid)).thenReturn(Optional.of(originalSale));
        when(personRepositoryPort.findById(clientId)).thenReturn(Optional.of(createClient(clientId)));

        ProductDomain returnedProduct = createProduct(10L, "Product 10", 10, new BigDecimal("5000.00"), outletId, 999L);
        ProductDomain replacementProduct = createProduct(12L, "Product 12", 10, new BigDecimal("5000.00"), outletId, 999L);

        when(productOutletRepositoryPort.findById(10L)).thenReturn(Optional.of(returnedProduct));
        when(productOutletRepositoryPort.findById(12L)).thenReturn(Optional.of(replacementProduct));

        when(masterTree.getById(999L)).thenReturn(null); // Unit measure node not found -> defaults to UND
        when(weightConversionService.isWeighable("UND")).thenReturn(false);
        when(weightConversionService.formatFromBaseUnit(1, "UND")).thenReturn("1 UN");

        ReturnItemDomain returnItem = new ReturnItemDomain(1L, 10L, 1, "1 UN", new BigDecimal("5000.00"), new BigDecimal("5000.00"), 999L);
        ReturnDomain returnDomain = ReturnDomain.create(100L, "DEFECTO", "Notas", "CAMBIO", 50L, employeePersonId, outletId, clientId, List.of(returnItem));
        when(returnRepositoryPort.save(any(ReturnDomain.class))).thenReturn(returnDomain);

        SaleItemDomain newSaleItem = new SaleItemDomain(2L, 12L, 1, "1 UN", new BigDecimal("5000.00"), new BigDecimal("5000.00"), 999L);
        SaleDomain newSale = SaleDomain.fromPersistence(
                101L, UUID.randomUUID(), new BigDecimal("5000.00"), BigDecimal.ZERO, BigDecimal.ZERO,
                1L, 1L, clientId, outletId, employeePersonId, LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), List.of(newSaleItem)
        );
        when(saleRepositoryPort.save(any(SaleDomain.class))).thenReturn(newSale);

        ExchangeResponse response = createExchangeUseCase.execute(request);
        assertNotNull(response);
        assertEquals(BigDecimal.ZERO.setScale(2), response.netDifference());
    }

    @Test
    @DisplayName("Should allow exchange across outlets when user has ROLE_ADMINTI")
    void shouldAllowExchangeAcrossOutletsWhenUserIsAdminTI() {
        CreateExchangeRequest request = new CreateExchangeRequest(
                saleUuid, "DEFECTO", "Notas",
                List.of(new ReturnItemRequest(10L, "1")),
                List.of(new ExchangeItemRequest(12L, "1")),
                1L, BigDecimal.ZERO, false
        );

        when(currentUserProvider.getCurrentUserContext()).thenReturn(new UserContext("adminTiUser", List.of("ROLE_ADMINTI"), 1L));
        when(currentUserProvider.hasRole("ROLE_ADMIN")).thenReturn(false);
        when(currentUserProvider.hasRole("ROLE_ADMINTI")).thenReturn(true);
        when(currentUserProvider.getCurrentOutletId()).thenReturn(1L);

        UserAvalonDomain userDomain = UserAvalonDomain.fromPersistenceBasic(5L, employeePersonId, "adminTiUser", 1L);
        when(userAvalonRepositoryPort.findByUserName("adminTiUser")).thenReturn(Optional.of(userDomain));

        setupCommonMasterData();

        SaleItemDomain item = new SaleItemDomain(1L, 10L, 1, "1 UN", new BigDecimal("5000.00"), new BigDecimal("5000.00"), 1L);
        SaleDomain originalSale = createOriginalSale(100L, 2L, clientId, item);
        when(saleRepositoryPort.findByCode(saleUuid)).thenReturn(Optional.of(originalSale));
        when(personRepositoryPort.findById(clientId)).thenReturn(Optional.of(createClient(clientId)));

        ProductDomain returnedProduct = createProduct(10L, "Jabon", 10, new BigDecimal("5000.00"), 2L, 1L);
        ProductDomain replacementProduct = createProduct(12L, "Jabon Especial", 5, new BigDecimal("5000.00"), 2L, 1L);

        when(productOutletRepositoryPort.findById(10L)).thenReturn(Optional.of(returnedProduct));
        when(productOutletRepositoryPort.findById(12L)).thenReturn(Optional.of(replacementProduct));

        when(masterTree.getById(1L)).thenReturn(new MasterRoot(1L, "UND", "Unidad", 0L, 1L));
        when(weightConversionService.isWeighable("UND")).thenReturn(false);
        when(weightConversionService.formatFromBaseUnit(1, "UND")).thenReturn("1 UN");

        ReturnItemDomain returnItem = new ReturnItemDomain(1L, 10L, 1, "1 UN", new BigDecimal("5000.00"), new BigDecimal("5000.00"), 1L);
        ReturnDomain returnDomain = ReturnDomain.create(100L, "DEFECTO", "Notas", "CAMBIO", 50L, employeePersonId, 2L, clientId, List.of(returnItem));
        when(returnRepositoryPort.save(any(ReturnDomain.class))).thenReturn(returnDomain);

        SaleItemDomain newSaleItem = new SaleItemDomain(2L, 12L, 1, "1 UN", new BigDecimal("5000.00"), new BigDecimal("5000.00"), 1L);
        SaleDomain newSale = SaleDomain.fromPersistence(
                101L, UUID.randomUUID(), new BigDecimal("5000.00"), BigDecimal.ZERO, BigDecimal.ZERO,
                1L, 1L, clientId, 2L, employeePersonId, LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), List.of(newSaleItem)
        );
        when(saleRepositoryPort.save(any(SaleDomain.class))).thenReturn(newSale);

        ExchangeResponse response = createExchangeUseCase.execute(request);
        assertNotNull(response);
        assertEquals(2L, response.newSaleDetail().outletId());
    }

    @Test
    @DisplayName("Should allow exchange when user is not admin and tenant outlet ID is null")
    void shouldAllowExchangeWhenUserIsNotAdminAndTenantOutletIdIsNull() {
        CreateExchangeRequest request = new CreateExchangeRequest(
                saleUuid, "DEFECTO", "Notas",
                List.of(new ReturnItemRequest(10L, "1")),
                List.of(new ExchangeItemRequest(12L, "1")),
                1L, BigDecimal.ZERO, false
        );

        when(currentUserProvider.getCurrentUserContext()).thenReturn(new UserContext("globalUser", List.of("ROLE_USER"), null));
        when(currentUserProvider.hasRole("ROLE_ADMIN")).thenReturn(false);
        when(currentUserProvider.hasRole("ROLE_ADMINTI")).thenReturn(false);
        when(currentUserProvider.getCurrentOutletId()).thenReturn(null);

        UserAvalonDomain userDomain = UserAvalonDomain.fromPersistenceBasic(5L, employeePersonId, "globalUser", null);
        when(userAvalonRepositoryPort.findByUserName("globalUser")).thenReturn(Optional.of(userDomain));

        setupCommonMasterData();

        SaleItemDomain item = new SaleItemDomain(1L, 10L, 1, "1 UN", new BigDecimal("5000.00"), new BigDecimal("5000.00"), 1L);
        SaleDomain originalSale = createOriginalSale(100L, outletId, clientId, item);
        when(saleRepositoryPort.findByCode(saleUuid)).thenReturn(Optional.of(originalSale));
        when(personRepositoryPort.findById(clientId)).thenReturn(Optional.of(createClient(clientId)));

        ProductDomain returnedProduct = createProduct(10L, "Jabon", 10, new BigDecimal("5000.00"), outletId, 1L);
        ProductDomain replacementProduct = createProduct(12L, "Jabon Especial", 5, new BigDecimal("5000.00"), outletId, 1L);

        when(productOutletRepositoryPort.findById(10L)).thenReturn(Optional.of(returnedProduct));
        when(productOutletRepositoryPort.findById(12L)).thenReturn(Optional.of(replacementProduct));

        when(masterTree.getById(1L)).thenReturn(new MasterRoot(1L, "UND", "Unidad", 0L, 1L));
        when(weightConversionService.isWeighable("UND")).thenReturn(false);
        when(weightConversionService.formatFromBaseUnit(1, "UND")).thenReturn("1 UN");

        ReturnItemDomain returnItem = new ReturnItemDomain(1L, 10L, 1, "1 UN", new BigDecimal("5000.00"), new BigDecimal("5000.00"), 1L);
        ReturnDomain returnDomain = ReturnDomain.create(100L, "DEFECTO", "Notas", "CAMBIO", 50L, employeePersonId, outletId, clientId, List.of(returnItem));
        when(returnRepositoryPort.save(any(ReturnDomain.class))).thenReturn(returnDomain);

        SaleItemDomain newSaleItem = new SaleItemDomain(2L, 12L, 1, "1 UN", new BigDecimal("5000.00"), new BigDecimal("5000.00"), 1L);
        SaleDomain newSale = SaleDomain.fromPersistence(
                101L, UUID.randomUUID(), new BigDecimal("5000.00"), BigDecimal.ZERO, BigDecimal.ZERO,
                1L, 1L, clientId, outletId, employeePersonId, LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), List.of(newSaleItem)
        );
        when(saleRepositoryPort.save(any(SaleDomain.class))).thenReturn(newSale);

        ExchangeResponse response = createExchangeUseCase.execute(request);
        assertNotNull(response);
        assertEquals(outletId, response.newSaleDetail().outletId());
    }

    @Test
    @DisplayName("Should handle null payment method node in MasterTree gracefully")
    void shouldHandleNullPaymentMethodNodeInMasterTree() {
        CreateExchangeRequest request = new CreateExchangeRequest(
                saleUuid, "DEFECTO", "Notas",
                List.of(new ReturnItemRequest(10L, "1")),
                List.of(new ExchangeItemRequest(12L, "1")),
                999L, BigDecimal.ZERO, false
        );

        setupUserContext("cashier1", "ROLE_CJTURNO", outletId);
        setupCommonMasterData();

        SaleItemDomain item = new SaleItemDomain(1L, 10L, 1, "1 UN", new BigDecimal("5000.00"), new BigDecimal("5000.00"), 1L);
        SaleDomain originalSale = createOriginalSale(100L, outletId, clientId, item);
        when(saleRepositoryPort.findByCode(saleUuid)).thenReturn(Optional.of(originalSale));
        when(personRepositoryPort.findById(clientId)).thenReturn(Optional.of(createClient(clientId)));

        ProductDomain returnedProduct = createProduct(10L, "Jabon", 10, new BigDecimal("5000.00"), outletId, 1L);
        ProductDomain replacementProduct = createProduct(12L, "Jabon Especial", 5, new BigDecimal("5000.00"), outletId, 1L);

        when(productOutletRepositoryPort.findById(10L)).thenReturn(Optional.of(returnedProduct));
        when(productOutletRepositoryPort.findById(12L)).thenReturn(Optional.of(replacementProduct));

        when(masterTree.getById(1L)).thenReturn(new MasterRoot(1L, "UND", "Unidad", 0L, 1L));
        when(masterTree.getById(999L)).thenReturn(null);
        when(weightConversionService.isWeighable("UND")).thenReturn(false);
        when(weightConversionService.formatFromBaseUnit(1, "UND")).thenReturn("1 UN");

        ReturnItemDomain returnItem = new ReturnItemDomain(1L, 10L, 1, "1 UN", new BigDecimal("5000.00"), new BigDecimal("5000.00"), 1L);
        ReturnDomain returnDomain = ReturnDomain.create(100L, "DEFECTO", "Notas", "CAMBIO", 50L, employeePersonId, outletId, clientId, List.of(returnItem));
        when(returnRepositoryPort.save(any(ReturnDomain.class))).thenReturn(returnDomain);

        SaleItemDomain newSaleItem = new SaleItemDomain(2L, 12L, 1, "1 UN", new BigDecimal("5000.00"), new BigDecimal("5000.00"), 1L);
        SaleDomain newSale = SaleDomain.fromPersistence(
                101L, UUID.randomUUID(), new BigDecimal("5000.00"), BigDecimal.ZERO, BigDecimal.ZERO,
                999L, 1L, clientId, outletId, employeePersonId, LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), List.of(newSaleItem)
        );
        when(saleRepositoryPort.save(any(SaleDomain.class))).thenReturn(newSale);

        ExchangeResponse response = createExchangeUseCase.execute(request);
        assertNotNull(response);
        assertEquals(BigDecimal.ZERO.setScale(2), response.netDifference());
        assertNull(response.newSaleDetail().paymentMethod().shortName());
    }

    @Test
    @DisplayName("Should process client surplus and pay full debt when surplus exceeds active debt")
    void shouldProcessSurplusForClient_AndPayFullDebtWhenSurplusExceedsDebt() {
        CreateExchangeRequest request = new CreateExchangeRequest(
                saleUuid, "DEFECTO", "Notas",
                List.of(new ReturnItemRequest(10L, "2")),
                List.of(new ExchangeItemRequest(12L, "1")),
                1L, BigDecimal.ZERO, false
        );

        setupUserContext("cashier1", "ROLE_CJTURNO", outletId);
        setupCommonMasterData();

        SaleItemDomain originalItem = new SaleItemDomain(1L, 10L, 2, "2 UN", new BigDecimal("5000.00"), new BigDecimal("10000.00"), 1L);
        SaleDomain originalSale = createOriginalSale(100L, outletId, clientId, originalItem);
        when(saleRepositoryPort.findByCode(saleUuid)).thenReturn(Optional.of(originalSale));
        when(personRepositoryPort.findById(clientId)).thenReturn(Optional.of(createClient(clientId)));

        ProductDomain returnedProduct = createProduct(10L, "Jabon", 10, new BigDecimal("5000.00"), outletId, 1L);
        ProductDomain replacementProduct = createProduct(12L, "Caramelo", 5, new BigDecimal("2000.00"), outletId, 1L);

        when(productOutletRepositoryPort.findById(10L)).thenReturn(Optional.of(returnedProduct));
        when(productOutletRepositoryPort.findById(12L)).thenReturn(Optional.of(replacementProduct));

        when(masterTree.getById(1L)).thenReturn(new MasterRoot(1L, "UND", "Unidad", 0L, 1L));
        when(weightConversionService.isWeighable("UND")).thenReturn(false);
        when(weightConversionService.formatFromBaseUnit(2, "UND")).thenReturn("2 UN");
        when(weightConversionService.formatFromBaseUnit(1, "UND")).thenReturn("1 UN");

        ReturnItemDomain returnItem = new ReturnItemDomain(1L, 10L, 2, "2 UN", new BigDecimal("5000.00"), new BigDecimal("10000.00"), 1L);
        ReturnDomain returnDomain = ReturnDomain.create(100L, "DEFECTO", "Notas", "CAMBIO", 50L, employeePersonId, outletId, clientId, List.of(returnItem));
        when(returnRepositoryPort.save(any(ReturnDomain.class))).thenReturn(returnDomain);

        CreditAccountDomain creditAccount = CreditAccountDomain.reconstruct(
                10L, clientId, outletId, new BigDecimal("150000.00"), new BigDecimal("3000.00"), 1L, LocalDateTime.now(), LocalDateTime.now()
        );
        when(creditRepositoryPort.findByClientIdAndOutletId(clientId, outletId)).thenReturn(Optional.of(creditAccount));
        when(creditRepositoryPort.save(any(CreditAccountDomain.class))).thenReturn(creditAccount);

        SaleItemDomain newSaleItem = new SaleItemDomain(2L, 12L, 1, "1 UN", new BigDecimal("2000.00"), new BigDecimal("2000.00"), 1L);
        SaleDomain newSale = SaleDomain.fromPersistence(
                101L, UUID.randomUUID(), new BigDecimal("2000.00"), BigDecimal.ZERO, BigDecimal.ZERO,
                1L, 1L, clientId, outletId, employeePersonId, LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), List.of(newSaleItem)
        );
        when(saleRepositoryPort.save(any(SaleDomain.class))).thenReturn(newSale);

        ExchangeResponse response = createExchangeUseCase.execute(request);

        assertNotNull(response);
        assertEquals(new BigDecimal("-8000.00"), response.netDifference());
        assertTrue(response.paymentStatusMessage().contains("aplicada para abonar a la deuda del cliente"));
        assertEquals(BigDecimal.ZERO, creditAccount.getCurrentDebt());

        verify(creditRepositoryPort, times(1)).save(any(CreditAccountDomain.class));
        verify(creditRepositoryPort, times(1)).save(any(CreditTransactionDomain.class));
    }

    @Test
    @DisplayName("Should process client surplus when credit account exists but debt is zero")
    void shouldProcessSurplusForClient_WhenCreditAccountExistsWithZeroDebt() {
        CreateExchangeRequest request = new CreateExchangeRequest(
                saleUuid, "DEFECTO", "Notas",
                List.of(new ReturnItemRequest(10L, "2")),
                List.of(new ExchangeItemRequest(12L, "1")),
                1L, BigDecimal.ZERO, false
        );

        setupUserContext("cashier1", "ROLE_CJTURNO", outletId);
        setupCommonMasterData();

        SaleItemDomain originalItem = new SaleItemDomain(1L, 10L, 2, "2 UN", new BigDecimal("5000.00"), new BigDecimal("10000.00"), 1L);
        SaleDomain originalSale = createOriginalSale(100L, outletId, clientId, originalItem);
        when(saleRepositoryPort.findByCode(saleUuid)).thenReturn(Optional.of(originalSale));
        when(personRepositoryPort.findById(clientId)).thenReturn(Optional.of(createClient(clientId)));

        ProductDomain returnedProduct = createProduct(10L, "Jabon", 10, new BigDecimal("5000.00"), outletId, 1L);
        ProductDomain replacementProduct = createProduct(12L, "Crema", 5, new BigDecimal("6000.00"), outletId, 1L);

        when(productOutletRepositoryPort.findById(10L)).thenReturn(Optional.of(returnedProduct));
        when(productOutletRepositoryPort.findById(12L)).thenReturn(Optional.of(replacementProduct));

        when(masterTree.getById(1L)).thenReturn(new MasterRoot(1L, "UND", "Unidad", 0L, 1L));
        when(weightConversionService.isWeighable("UND")).thenReturn(false);
        when(weightConversionService.formatFromBaseUnit(2, "UND")).thenReturn("2 UN");
        when(weightConversionService.formatFromBaseUnit(1, "UND")).thenReturn("1 UN");

        ReturnItemDomain returnItem = new ReturnItemDomain(1L, 10L, 2, "2 UN", new BigDecimal("5000.00"), new BigDecimal("10000.00"), 1L);
        ReturnDomain returnDomain = ReturnDomain.create(100L, "DEFECTO", "Notas", "CAMBIO", 50L, employeePersonId, outletId, clientId, List.of(returnItem));
        when(returnRepositoryPort.save(any(ReturnDomain.class))).thenReturn(returnDomain);

        CreditAccountDomain zeroDebtAccount = CreditAccountDomain.reconstruct(
                10L, clientId, outletId, new BigDecimal("150000.00"), BigDecimal.ZERO, 1L, LocalDateTime.now(), LocalDateTime.now()
        );
        when(creditRepositoryPort.findByClientIdAndOutletId(clientId, outletId)).thenReturn(Optional.of(zeroDebtAccount));

        SaleItemDomain newSaleItem = new SaleItemDomain(2L, 12L, 1, "1 UN", new BigDecimal("6000.00"), new BigDecimal("6000.00"), 1L);
        SaleDomain newSale = SaleDomain.fromPersistence(
                101L, UUID.randomUUID(), new BigDecimal("6000.00"), BigDecimal.ZERO, BigDecimal.ZERO,
                1L, 1L, clientId, outletId, employeePersonId, LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), List.of(newSaleItem)
        );
        when(saleRepositoryPort.save(any(SaleDomain.class))).thenReturn(newSale);

        ExchangeResponse response = createExchangeUseCase.execute(request);

        assertNotNull(response);
        assertEquals(new BigDecimal("-4000.00"), response.netDifference());
        assertTrue(response.paymentStatusMessage().contains("entregada al cliente"));

        verify(creditRepositoryPort, never()).save(any(CreditAccountDomain.class));
    }

    @Test
    @DisplayName("Should handle weighable conversion factor for L unit code in replacement product")
    void shouldHandleWeighableFactors_L_ForReplacementProduct() {
        CreateExchangeRequest request = new CreateExchangeRequest(
                saleUuid, "DEFECTO", "Notas",
                List.of(new ReturnItemRequest(10L, "1")),
                List.of(new ExchangeItemRequest(12L, "1000")),
                1L, BigDecimal.ZERO, false
        );

        setupUserContext("cashier1", "ROLE_CJTURNO", outletId);
        setupCommonMasterData();

        SaleItemDomain originalItem = new SaleItemDomain(1L, 10L, 1, "1 UN", new BigDecimal("3000.00"), new BigDecimal("3000.00"), 1L);
        SaleDomain originalSale = createOriginalSale(100L, outletId, clientId, originalItem);
        when(saleRepositoryPort.findByCode(saleUuid)).thenReturn(Optional.of(originalSale));
        when(personRepositoryPort.findById(clientId)).thenReturn(Optional.of(createClient(clientId)));

        ProductDomain returnedProduct = createProduct(10L, "Aceite 1UN", 10, new BigDecimal("3000.00"), outletId, 1L);
        ProductDomain replacementProduct = createProduct(12L, "Leche L", 5000, new BigDecimal("3000.00"), outletId, 5L);

        when(productOutletRepositoryPort.findById(10L)).thenReturn(Optional.of(returnedProduct));
        when(productOutletRepositoryPort.findById(12L)).thenReturn(Optional.of(replacementProduct));

        when(masterTree.getById(1L)).thenReturn(new MasterRoot(1L, "UND", "Unidad", 0L, 1L));
        when(masterTree.getById(5L)).thenReturn(new MasterRoot(5L, "L", "Litro", 0L, 1L));
        when(weightConversionService.isWeighable("UND")).thenReturn(false);
        when(weightConversionService.isWeighable("L")).thenReturn(true);
        when(weightConversionService.convertToBaseUnit(any(BigDecimal.class), eq("L"))).thenReturn(1000);
        when(weightConversionService.formatFromBaseUnit(1, "UND")).thenReturn("1 UN");
        when(weightConversionService.formatFromBaseUnit(1000, "L")).thenReturn("1000 L");

        ReturnItemDomain returnItem = new ReturnItemDomain(1L, 10L, 1, "1 UN", new BigDecimal("3000.00"), new BigDecimal("3000.00"), 1L);
        ReturnDomain returnDomain = ReturnDomain.create(100L, "DEFECTO", "Notas", "CAMBIO", 50L, employeePersonId, outletId, clientId, List.of(returnItem));
        when(returnRepositoryPort.save(any(ReturnDomain.class))).thenReturn(returnDomain);

        SaleItemDomain newSaleItem = new SaleItemDomain(2L, 12L, 1000, "1000 L", new BigDecimal("3000.00"), new BigDecimal("3000.00"), 5L);
        SaleDomain newSale = SaleDomain.fromPersistence(
                101L, UUID.randomUUID(), new BigDecimal("3000.00"), BigDecimal.ZERO, BigDecimal.ZERO,
                1L, 1L, clientId, outletId, employeePersonId, LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), List.of(newSaleItem)
        );
        when(saleRepositoryPort.save(any(SaleDomain.class))).thenReturn(newSale);

        ExchangeResponse response = createExchangeUseCase.execute(request);
        assertNotNull(response);
        assertEquals(new BigDecimal("3000.00"), response.totalReturned());
        assertEquals(new BigDecimal("3000.00"), response.totalNewItems());
        assertEquals(BigDecimal.ZERO.setScale(2), response.netDifference());
    }
}
