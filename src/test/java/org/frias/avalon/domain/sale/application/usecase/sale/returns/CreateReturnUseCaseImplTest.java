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
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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

    private final UUID defaultSaleUuid = UUID.randomUUID();
    private final Long outletId = 1L;
    private final Long clientId = 20L;
    private final Long employeePersonId = 15L;
    private final Long productId = 10L;

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

    private void setupBaseMocks() {
        UserContext userContext = new UserContext("cashier1", List.of("ROLE_CJTURNO"), outletId);
        when(currentUserProvider.getCurrentUserContext()).thenReturn(userContext);
        when(currentUserProvider.hasRole("ROLE_ADMIN")).thenReturn(false);
        when(currentUserProvider.hasRole("ROLE_ADMINTI")).thenReturn(false);
        when(currentUserProvider.getCurrentOutletId()).thenReturn(outletId);

        SaleItemDomain saleItem = new SaleItemDomain(
                1L, productId, 5, "5 UN", new BigDecimal("3500.00"), new BigDecimal("17500.00"), 1L
        );
        SaleDomain originalSale = SaleDomain.fromPersistence(
                100L, defaultSaleUuid, new BigDecimal("17500.00"), new BigDecimal("20000.00"), new BigDecimal("2500.00"),
                5L, outletId, clientId, 1L, employeePersonId, LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), List.of(saleItem)
        );
        when(saleRepositoryPort.findByCode(defaultSaleUuid)).thenReturn(Optional.of(originalSale));

        UserAvalonDomain userDomain = UserAvalonDomain.fromPersistenceBasic(5L, employeePersonId, "cashier1", 1L);
        when(userAvalonRepositoryPort.findByUserName("cashier1")).thenReturn(Optional.of(userDomain));

        PersonDomain client = PersonDomain.createFromEntity(
                clientId, "12345678", "JUAN", "PEREZ", "CALLE 1",
                1L, 1L, 5551234L, "juan@email.com", 1L, LocalDateTime.now(), LocalDateTime.now()
        );
        when(personRepositoryPort.findById(clientId)).thenReturn(Optional.of(client));

        when(masterDataRepositoryPort.getIdByCode("DEV")).thenReturn(50L);

        MasterTree masterTree = mock(MasterTree.class);
        when(masterTreeProvider.getTree()).thenReturn(masterTree);
        when(masterTree.getById(1L)).thenReturn(new MasterRoot(1L, "UND", "Unidad", 0L, 1L));

        ProductDomain product = ProductDomain.fromPersistence(
                productId, "Jabon de Tocador", "Jabon 100g", 10, 1L, "", new BigDecimal("3500.00"), outletId, 1L, LocalDateTime.now(), LocalDateTime.now()
        );
        when(productOutletRepositoryPort.findById(productId)).thenReturn(Optional.of(product));
        when(weightConversionService.isWeighable("UND")).thenReturn(false);
        when(weightConversionService.formatFromBaseUnit(2, "UND")).thenReturn("2 UN");
    }

    @Nested
    @DisplayName("Original Sale & Security Validations")
    class SecurityAndSaleValidationTests {

        @Test
        @DisplayName("Should throw ResourceNotFoundException when original sale code does not exist")
        void shouldThrowExceptionWhenSaleNotFound() {
            CreateReturnRequest request = new CreateReturnRequest(
                    defaultSaleUuid, "DEFECTO", "Notas", "REEMBOLSO",
                    List.of(new ReturnItemRequest(productId, "1")), false
            );

            when(currentUserProvider.getCurrentUserContext()).thenReturn(new UserContext("cashier1", List.of("ROLE_CJTURNO"), outletId));
            when(currentUserProvider.hasRole(anyString())).thenReturn(false);
            when(saleRepositoryPort.findByCode(defaultSaleUuid)).thenReturn(Optional.empty());

            ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                    () -> createReturnUseCase.execute(request));
            assertTrue(ex.getMessage().contains("No se encontró ninguna venta con el código"));
        }

        @Test
        @DisplayName("Should throw BusinessException when non-admin employee tries to process return for another outlet")
        void shouldThrowExceptionWhenOutletMismatchForNonAdmin() {
            CreateReturnRequest request = new CreateReturnRequest(
                    defaultSaleUuid, "DEFECTO", "Notas", "REEMBOLSO",
                    List.of(new ReturnItemRequest(productId, "1")), false
            );

            UserContext userContext = new UserContext("cashier1", List.of("ROLE_CJTURNO"), 1L);
            when(currentUserProvider.getCurrentUserContext()).thenReturn(userContext);
            when(currentUserProvider.hasRole("ROLE_ADMIN")).thenReturn(false);
            when(currentUserProvider.hasRole("ROLE_ADMINTI")).thenReturn(false);
            when(currentUserProvider.getCurrentOutletId()).thenReturn(1L);

            UserAvalonDomain empUser = UserAvalonDomain.fromPersistenceBasic(5L, employeePersonId, "cashier1", 1L);
            when(userAvalonRepositoryPort.findByUserName("cashier1")).thenReturn(Optional.of(empUser));

            SaleDomain saleFromOtherOutlet = SaleDomain.fromPersistence(
                    100L, defaultSaleUuid, new BigDecimal("17500.00"), new BigDecimal("20000.00"), new BigDecimal("2500.00"),
                    5L, 1L, clientId, 99L, employeePersonId, LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), List.of()
            );
            when(saleRepositoryPort.findByCode(defaultSaleUuid)).thenReturn(Optional.of(saleFromOtherOutlet));

            BusinessException ex = assertThrows(BusinessException.class, () -> createReturnUseCase.execute(request));
            assertEquals("Acceso denegado: Esta venta pertenece a otra tienda.", ex.getMessage());
        }

        @Test
        @DisplayName("Should allow processing return when user has ROLE_ADMIN even if outlet differs")
        void shouldAllowProcessingReturnWhenUserIsAdmin() {
            setupBaseMocks();
            when(currentUserProvider.hasRole("ROLE_ADMIN")).thenReturn(true);
            when(currentUserProvider.getCurrentOutletId()).thenReturn(99L);

            ReturnItemDomain returnItem = new ReturnItemDomain(
                    1L, productId, 2, "2 UN", new BigDecimal("3500.00"), new BigDecimal("7000.00"), 1L
            );
            ReturnDomain returnDomain = ReturnDomain.create(
                    100L, "DEFECTO", "Notas", "REEMBOLSO", 50L, employeePersonId, outletId, clientId, List.of(returnItem)
            );
            when(returnRepositoryPort.save(any())).thenReturn(returnDomain);

            CreateReturnRequest request = new CreateReturnRequest(
                    defaultSaleUuid, "DEFECTO", "Notas", "REEMBOLSO",
                    List.of(new ReturnItemRequest(productId, "2")), false
            );

            assertDoesNotThrow(() -> createReturnUseCase.execute(request));
        }

        @Test
        @DisplayName("Should allow processing return when tenantOutletId is null")
        void shouldAllowProcessingReturnWhenTenantOutletIdIsNull() {
            setupBaseMocks();
            when(currentUserProvider.hasRole("ROLE_ADMIN")).thenReturn(false);
            when(currentUserProvider.hasRole("ROLE_ADMINTI")).thenReturn(false);
            when(currentUserProvider.getCurrentOutletId()).thenReturn(null);

            ReturnItemDomain returnItem = new ReturnItemDomain(
                    1L, productId, 2, "2 UN", new BigDecimal("3500.00"), new BigDecimal("7000.00"), 1L
            );
            ReturnDomain returnDomain = ReturnDomain.create(
                    100L, "DEFECTO", "Notas", "REEMBOLSO", 50L, employeePersonId, outletId, clientId, List.of(returnItem)
            );
            when(returnRepositoryPort.save(any())).thenReturn(returnDomain);

            CreateReturnRequest request = new CreateReturnRequest(
                    defaultSaleUuid, "DEFECTO", "Notas", "REEMBOLSO",
                    List.of(new ReturnItemRequest(productId, "2")), false
            );

            assertDoesNotThrow(() -> createReturnUseCase.execute(request));
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when authenticated user domain is not found")
        void shouldThrowExceptionWhenUserDomainNotFound() {
            setupBaseMocks();
            when(userAvalonRepositoryPort.findByUserName("cashier1")).thenReturn(Optional.empty());

            CreateReturnRequest request = new CreateReturnRequest(
                    defaultSaleUuid, "DEFECTO", "Notas", "REEMBOLSO",
                    List.of(new ReturnItemRequest(productId, "1")), false
            );

            ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                    () -> createReturnUseCase.execute(request));
            assertEquals("Usuario autenticado no encontrado", ex.getMessage());
        }

        @Test
        @DisplayName("Should throw BusinessException when authenticated user has no personId")
        void shouldThrowExceptionWhenUserHasNoPersonId() {
            setupBaseMocks();
            UserAvalonDomain userWithoutPerson = UserAvalonDomain.fromPersistenceBasic(5L, null, "cashier1", 1L);
            when(userAvalonRepositoryPort.findByUserName("cashier1")).thenReturn(Optional.of(userWithoutPerson));

            CreateReturnRequest request = new CreateReturnRequest(
                    defaultSaleUuid, "DEFECTO", "Notas", "REEMBOLSO",
                    List.of(new ReturnItemRequest(productId, "1")), false
            );

            BusinessException ex = assertThrows(BusinessException.class, () -> createReturnUseCase.execute(request));
            assertEquals("El usuario actual no tiene un registro de persona (empleado) asociado", ex.getMessage());
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when sale client is not found")
        void shouldThrowExceptionWhenClientNotFound() {
            setupBaseMocks();
            when(personRepositoryPort.findById(clientId)).thenReturn(Optional.empty());

            CreateReturnRequest request = new CreateReturnRequest(
                    defaultSaleUuid, "DEFECTO", "Notas", "REEMBOLSO",
                    List.of(new ReturnItemRequest(productId, "1")), false
            );

            ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                    () -> createReturnUseCase.execute(request));
            assertEquals("Cliente de la venta original no encontrado", ex.getMessage());
        }

        @Test
        @DisplayName("Should throw IllegalStateException when DEV status ID is not found in MasterData")
        void shouldThrowExceptionWhenDevStatusIdNotFound() {
            setupBaseMocks();
            when(masterDataRepositoryPort.getIdByCode("DEV")).thenReturn(null);

            CreateReturnRequest request = new CreateReturnRequest(
                    defaultSaleUuid, "DEFECTO", "Notas", "REEMBOLSO",
                    List.of(new ReturnItemRequest(productId, "1")), false
            );

            IllegalStateException ex = assertThrows(IllegalStateException.class,
                    () -> createReturnUseCase.execute(request));
            assertTrue(ex.getMessage().contains("Estado 'DEV' (Devuelto) no encontrado en MasterData"));
        }
    }

    @Nested
    @DisplayName("Product & Quantity Validation Tests")
    class ProductAndQuantityValidationTests {

        @Test
        @DisplayName("Should throw BusinessException when product is not in original sale")
        void shouldThrowExceptionWhenProductNotInOriginalSale() {
            setupBaseMocks();
            CreateReturnRequest request = new CreateReturnRequest(
                    defaultSaleUuid, "DEFECTO", "Notas", "REEMBOLSO",
                    List.of(new ReturnItemRequest(999L, "1")), false
            );

            BusinessException ex = assertThrows(BusinessException.class, () -> createReturnUseCase.execute(request));
            assertTrue(ex.getMessage().contains("no está en la venta original"));
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when product is not found in catalog")
        void shouldThrowExceptionWhenProductNotFoundInCatalog() {
            setupBaseMocks();
            when(productOutletRepositoryPort.findById(productId)).thenReturn(Optional.empty());

            CreateReturnRequest request = new CreateReturnRequest(
                    defaultSaleUuid, "DEFECTO", "Notas", "REEMBOLSO",
                    List.of(new ReturnItemRequest(productId, "1")), false
            );

            ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                    () -> createReturnUseCase.execute(request));
            assertTrue(ex.getMessage().contains("Producto 10 no encontrado"));
        }

        @Test
        @DisplayName("Should throw BusinessException when weighable product has invalid quantity format")
        void shouldThrowExceptionWhenWeighableQuantityInvalid() {
            setupBaseMocks();
            MasterTree masterTree = masterTreeProvider.getTree();
            when(masterTree.getById(1L)).thenReturn(new MasterRoot(1L, "KG", "Kilogramos", 0L, 1L));
            when(weightConversionService.isWeighable("KG")).thenReturn(true);

            CreateReturnRequest request = new CreateReturnRequest(
                    defaultSaleUuid, "DEFECTO", "Notas", "REEMBOLSO",
                    List.of(new ReturnItemRequest(productId, "invalid_num")), false
            );

            BusinessException ex = assertThrows(BusinessException.class, () -> createReturnUseCase.execute(request));
            assertTrue(ex.getMessage().contains("Cantidad inválida para producto pesable"));
        }

        @Test
        @DisplayName("Should throw BusinessException when non-weighable product has invalid integer quantity format")
        void shouldThrowExceptionWhenNonWeighableQuantityInvalid() {
            setupBaseMocks();
            CreateReturnRequest request = new CreateReturnRequest(
                    defaultSaleUuid, "DEFECTO", "Notas", "REEMBOLSO",
                    List.of(new ReturnItemRequest(productId, "2.5")), false
            );

            BusinessException ex = assertThrows(BusinessException.class, () -> createReturnUseCase.execute(request));
            assertTrue(ex.getMessage().contains("La cantidad debe ser un entero para"));
        }

        @Test
        @DisplayName("Should throw BusinessException when return quantity in base units is zero or negative")
        void shouldThrowExceptionWhenQuantityIsZeroOrNegative() {
            setupBaseMocks();
            CreateReturnRequest request = new CreateReturnRequest(
                    defaultSaleUuid, "DEFECTO", "Notas", "REEMBOLSO",
                    List.of(new ReturnItemRequest(productId, "0")), false
            );

            BusinessException ex = assertThrows(BusinessException.class, () -> createReturnUseCase.execute(request));
            assertTrue(ex.getMessage().contains("La cantidad a devolver debe ser mayor a cero"));
        }

        @Test
        @DisplayName("Should throw BusinessException when return quantity exceeds originally sold quantity")
        void shouldThrowExceptionWhenQuantityExceedsSold() {
            setupBaseMocks();
            CreateReturnRequest request = new CreateReturnRequest(
                    defaultSaleUuid, "DEFECTO", "Notas", "REEMBOLSO",
                    List.of(new ReturnItemRequest(productId, "10")), false
            );

            BusinessException ex = assertThrows(BusinessException.class, () -> createReturnUseCase.execute(request));
            assertTrue(ex.getMessage().contains("supera lo vendido"));
        }
    }

    @Nested
    @DisplayName("Stock Reintegration & Return Reasons (DEFECTO, INCORRECTO, OTRO)")
    class StockReintegrationAndReasonsTests {

        @Test
        @DisplayName("Should process return with DEFECTO reason, reintegrate stock and save ReturnDomain")
        void shouldProcessReturnWithReasonDefecto() {
            setupBaseMocks();
            CreateReturnRequest request = new CreateReturnRequest(
                    defaultSaleUuid, "DEFECTO", "Producto defectuoso", "REEMBOLSO",
                    List.of(new ReturnItemRequest(productId, "2")), true
            );

            ReturnItemDomain returnItem = new ReturnItemDomain(
                    1L, productId, 2, "2 UN", new BigDecimal("3500.00"), new BigDecimal("7000.00"), 1L
            );
            ReturnDomain returnDomain = ReturnDomain.create(
                    100L, "DEFECTO", "Producto defectuoso", "REEMBOLSO",
                    50L, employeePersonId, outletId, clientId, List.of(returnItem)
            );
            when(returnRepositoryPort.save(any(ReturnDomain.class))).thenReturn(returnDomain);

            ReturnResponse response = createReturnUseCase.execute(request);

            assertNotNull(response);
            assertEquals("DEFECTO", response.reason());
            assertEquals(defaultSaleUuid, response.originalSaleCode());

            ArgumentCaptor<ProductDomain> productCaptor = ArgumentCaptor.forClass(ProductDomain.class);
            verify(productOutletRepositoryPort).save(productCaptor.capture());
            assertEquals(12, productCaptor.getValue().getStock()); // Initial 10 + 2 restored
        }

        @Test
        @DisplayName("Should process return with INCORRECTO reason and fallback unit when unit measure node is null")
        void shouldProcessReturnWithReasonIncorrectoAndNullUnitNode() {
            setupBaseMocks();
            MasterTree masterTree = masterTreeProvider.getTree();
            when(masterTree.getById(1L)).thenReturn(null); // Unit measure node is null -> fallback "UND"

            CreateReturnRequest request = new CreateReturnRequest(
                    defaultSaleUuid, "INCORRECTO", "Talla equivocada", "REEMBOLSO",
                    List.of(new ReturnItemRequest(productId, "1")), false
            );

            ReturnItemDomain returnItem = new ReturnItemDomain(
                    1L, productId, 1, "1 UN", new BigDecimal("3500.00"), new BigDecimal("3500.00"), 1L
            );
            ReturnDomain returnDomain = ReturnDomain.create(
                    100L, "INCORRECTO", "Talla equivocada", "REEMBOLSO",
                    50L, employeePersonId, outletId, clientId, List.of(returnItem)
            );
            when(returnRepositoryPort.save(any(ReturnDomain.class))).thenReturn(returnDomain);

            ReturnResponse response = createReturnUseCase.execute(request);

            assertNotNull(response);
            assertEquals("INCORRECTO", response.reason());

            ArgumentCaptor<ProductDomain> productCaptor = ArgumentCaptor.forClass(ProductDomain.class);
            verify(productOutletRepositoryPort).save(productCaptor.capture());
            assertEquals(11, productCaptor.getValue().getStock()); // Initial 10 + 1 restored
        }

        @Test
        @DisplayName("Should process return with OTRO reason for weighable product in KG unit")
        void shouldProcessReturnWithReasonOtroAndKgWeighableProduct() {
            setupBaseMocks();
            MasterTree masterTree = masterTreeProvider.getTree();
            when(masterTree.getById(1L)).thenReturn(new MasterRoot(1L, "KG", "Kilogramos", 0L, 1L));
            when(weightConversionService.isWeighable("KG")).thenReturn(true);
            when(weightConversionService.convertToBaseUnit(new BigDecimal("1.5"), "KG")).thenReturn(1500);
            when(weightConversionService.formatFromBaseUnit(1500, "KG")).thenReturn("1.5 KG");

            SaleItemDomain saleItemKg = new SaleItemDomain(
                    1L, productId, 5000, "5 KG", new BigDecimal("3500.00"), new BigDecimal("17500.00"), 1L
            );
            SaleDomain saleKg = SaleDomain.fromPersistence(
                    100L, defaultSaleUuid, new BigDecimal("17500.00"), new BigDecimal("20000.00"), new BigDecimal("2500.00"),
                    5L, outletId, clientId, 1L, employeePersonId, LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), List.of(saleItemKg)
            );
            when(saleRepositoryPort.findByCode(defaultSaleUuid)).thenReturn(Optional.of(saleKg));

            ProductDomain kgProduct = ProductDomain.fromPersistence(
                    productId, "Carne de Res", "Carne molida", 10000, 1L, "", new BigDecimal("3500.00"), outletId, 1L, LocalDateTime.now(), LocalDateTime.now()
            );
            when(productOutletRepositoryPort.findById(productId)).thenReturn(Optional.of(kgProduct));

            CreateReturnRequest request = new CreateReturnRequest(
                    defaultSaleUuid, "OTRO", "Cambio de decision", "REEMBOLSO",
                    List.of(new ReturnItemRequest(productId, "1,5")), false
            );

            ReturnItemDomain returnItem = new ReturnItemDomain(
                    1L, productId, 1500, "1.5 KG", new BigDecimal("3500.00"), new BigDecimal("5250.00"), 1L
            );
            ReturnDomain returnDomain = ReturnDomain.create(
                    100L, "OTRO", "Cambio de decision", "REEMBOLSO",
                    50L, employeePersonId, outletId, clientId, List.of(returnItem)
            );
            when(returnRepositoryPort.save(any(ReturnDomain.class))).thenReturn(returnDomain);

            ReturnResponse response = createReturnUseCase.execute(request);

            assertNotNull(response);
            assertEquals("OTRO", response.reason());

            ArgumentCaptor<ProductDomain> productCaptor = ArgumentCaptor.forClass(ProductDomain.class);
            verify(productOutletRepositoryPort).save(productCaptor.capture());
            assertEquals(11500, productCaptor.getValue().getStock()); // Initial 10000 + 1500 restored
        }

        @Test
        @DisplayName("Should calculate subtotal correctly for weighable product in LB unit")
        void shouldCalculateSubtotalForWeighableProductInLb() {
            setupBaseMocks();
            MasterTree masterTree = masterTreeProvider.getTree();
            when(masterTree.getById(1L)).thenReturn(new MasterRoot(1L, "LB", "Libras", 0L, 1L));
            when(weightConversionService.isWeighable("LB")).thenReturn(true);
            when(weightConversionService.convertToBaseUnit(new BigDecimal("2.0"), "LB")).thenReturn(907);
            when(weightConversionService.formatFromBaseUnit(907, "LB")).thenReturn("2 LB");

            SaleItemDomain saleItemLb = new SaleItemDomain(
                    1L, productId, 2000, "4.4 LB", new BigDecimal("10.00"), new BigDecimal("20.00"), 1L
            );
            SaleDomain saleLb = SaleDomain.fromPersistence(
                    100L, defaultSaleUuid, new BigDecimal("20.00"), new BigDecimal("20.00"), BigDecimal.ZERO,
                    5L, outletId, clientId, 1L, employeePersonId, LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), List.of(saleItemLb)
            );
            when(saleRepositoryPort.findByCode(defaultSaleUuid)).thenReturn(Optional.of(saleLb));

            ProductDomain lbProduct = ProductDomain.fromPersistence(
                    productId, "Queso LB", "Queso fresco", 5000, 1L, "", new BigDecimal("10.00"), outletId, 1L, LocalDateTime.now(), LocalDateTime.now()
            );
            when(productOutletRepositoryPort.findById(productId)).thenReturn(Optional.of(lbProduct));

            CreateReturnRequest request = new CreateReturnRequest(
                    defaultSaleUuid, "DEFECTO", "Empaque roto", "REEMBOLSO",
                    List.of(new ReturnItemRequest(productId, "2.0")), false
            );

            ReturnItemDomain returnItem = new ReturnItemDomain(
                    1L, productId, 907, "2 LB", new BigDecimal("10.00"), new BigDecimal("20.00"), 1L
            );
            ReturnDomain returnDomain = ReturnDomain.create(
                    100L, "DEFECTO", "Empaque roto", "REEMBOLSO",
                    50L, employeePersonId, outletId, clientId, List.of(returnItem)
            );
            when(returnRepositoryPort.save(any(ReturnDomain.class))).thenReturn(returnDomain);

            ReturnResponse response = createReturnUseCase.execute(request);
            assertNotNull(response);
            verify(productOutletRepositoryPort).save(any(ProductDomain.class));
        }

        @Test
        @DisplayName("Should calculate subtotal correctly for weighable product with default unit factor (e.g., GRAMOS)")
        void shouldCalculateSubtotalForWeighableProductInDefaultUnit() {
            setupBaseMocks();
            MasterTree masterTree = masterTreeProvider.getTree();
            when(masterTree.getById(1L)).thenReturn(new MasterRoot(1L, "GRAMOS", "Gramos", 0L, 1L));
            when(weightConversionService.isWeighable("GRAMOS")).thenReturn(true);
            when(weightConversionService.convertToBaseUnit(new BigDecimal("100"), "GRAMOS")).thenReturn(100);
            when(weightConversionService.formatFromBaseUnit(100, "GRAMOS")).thenReturn("100 G");

            SaleItemDomain saleItemG = new SaleItemDomain(
                    1L, productId, 500, "500 G", new BigDecimal("5.00"), new BigDecimal("2500.00"), 1L
            );
            SaleDomain saleG = SaleDomain.fromPersistence(
                    100L, defaultSaleUuid, new BigDecimal("2500.00"), new BigDecimal("2500.00"), BigDecimal.ZERO,
                    5L, outletId, clientId, 1L, employeePersonId, LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), List.of(saleItemG)
            );
            when(saleRepositoryPort.findByCode(defaultSaleUuid)).thenReturn(Optional.of(saleG));

            ProductDomain gProduct = ProductDomain.fromPersistence(
                    productId, "Especias", "Pimienta", 1000, 1L, "", new BigDecimal("5.00"), outletId, 1L, LocalDateTime.now(), LocalDateTime.now()
            );
            when(productOutletRepositoryPort.findById(productId)).thenReturn(Optional.of(gProduct));

            CreateReturnRequest request = new CreateReturnRequest(
                    defaultSaleUuid, "DEFECTO", "Contaminado", "REEMBOLSO",
                    List.of(new ReturnItemRequest(productId, "100")), false
            );

            ReturnItemDomain returnItem = new ReturnItemDomain(
                    1L, productId, 100, "100 G", new BigDecimal("5.00"), new BigDecimal("500.00"), 1L
            );
            ReturnDomain returnDomain = ReturnDomain.create(
                    100L, "DEFECTO", "Contaminado", "REEMBOLSO",
                    50L, employeePersonId, outletId, clientId, List.of(returnItem)
            );
            when(returnRepositoryPort.save(any(ReturnDomain.class))).thenReturn(returnDomain);

            ReturnResponse response = createReturnUseCase.execute(request);
            assertNotNull(response);
            verify(productOutletRepositoryPort).save(any(ProductDomain.class));
        }
    }

    @Nested
    @DisplayName("Resolutions & Protection Rules (REEMBOLSO, NOTA_CREDITO, CAMBIO)")
    class ResolutionTypesAndProtectionRulesTests {

        @Test
        @DisplayName("Should throw BusinessException when REEMBOLSO is requested on a FIADO (credit) sale")
        void shouldThrowExceptionWhenReembolsoOnFiadoSale() {
            setupBaseMocks();
            MasterTree masterTree = masterTreeProvider.getTree();
            when(masterTree.getById(5L)).thenReturn(new MasterRoot(5L, "FIA", "Fiado / Credito", 0L, 1L));

            CreateReturnRequest request = new CreateReturnRequest(
                    defaultSaleUuid, "DEFECTO", "Notas", "REEMBOLSO",
                    List.of(new ReturnItemRequest(productId, "1")), false
            );

            BusinessException ex = assertThrows(BusinessException.class, () -> createReturnUseCase.execute(request));
            assertTrue(ex.getMessage().contains("No se permite reembolso en efectivo de una venta comprada a crédito/fiado (FIA)"));
        }

        @Test
        @DisplayName("Should process NOTA_CREDITO resolution when client has existing credit account with positive debt")
        void shouldProcessNotaCreditoWithExistingCreditAccountAndDebt() {
            setupBaseMocks();
            CreateReturnRequest request = new CreateReturnRequest(
                    defaultSaleUuid, "DEFECTO", "Notas", "NOTA_CREDITO",
                    List.of(new ReturnItemRequest(productId, "2")), false
            );

            CreditAccountDomain creditAccount = CreditAccountDomain.reconstruct(
                    300L, clientId, outletId, new BigDecimal("150000"),
                    new BigDecimal("10000.00"), 1L, LocalDateTime.now(), LocalDateTime.now()
            );
            when(creditRepositoryPort.findByClientIdAndOutletId(clientId, outletId))
                    .thenReturn(Optional.of(creditAccount));

            ReturnItemDomain returnItem = new ReturnItemDomain(
                    1L, productId, 2, "2 UN", new BigDecimal("3500.00"), new BigDecimal("7000.00"), 1L
            );
            ReturnDomain returnDomain = ReturnDomain.create(
                    100L, "DEFECTO", "Notas", "NOTA_CREDITO",
                    50L, employeePersonId, outletId, clientId, List.of(returnItem)
            );
            when(returnRepositoryPort.save(any(ReturnDomain.class))).thenReturn(returnDomain);

            ReturnResponse response = createReturnUseCase.execute(request);

            assertNotNull(response);
            assertEquals("NOTA_CREDITO", response.resolutionType());

            verify(creditRepositoryPort, times(1)).save(creditAccount);
            assertEquals(new BigDecimal("3000.00"), creditAccount.getCurrentDebt()); // Debt reduced: 10000 - 7000 = 3000

            ArgumentCaptor<CreditTransactionDomain> txnCaptor = ArgumentCaptor.forClass(CreditTransactionDomain.class);
            verify(creditRepositoryPort).save(txnCaptor.capture());
            CreditTransactionDomain txn = txnCaptor.getValue();
            assertEquals("RETURN_CREDIT", txn.getType());
            assertEquals(new BigDecimal("7000.00"), txn.getAmount());
            assertEquals(new BigDecimal("10000.00"), txn.getPreviousDebt());
            assertEquals(new BigDecimal("3000.00"), txn.getNewDebt());
        }

        @Test
        @DisplayName("Should process NOTA_CREDITO resolution and create new credit account when none exists")
        void shouldProcessNotaCreditoCreatingNewAccountWhenNoneExists() {
            setupBaseMocks();
            when(masterDataRepositoryPort.getIdByCode("ACT")).thenReturn(200L);
            when(creditRepositoryPort.findByClientIdAndOutletId(clientId, outletId))
                    .thenReturn(Optional.empty());

            CreditAccountDomain newlyCreatedAccount = CreditAccountDomain.reconstruct(
                    400L, clientId, outletId, new BigDecimal("150000"),
                    BigDecimal.ZERO, 200L, LocalDateTime.now(), LocalDateTime.now()
            );
            when(creditRepositoryPort.save(any(CreditAccountDomain.class))).thenReturn(newlyCreatedAccount);

            CreateReturnRequest request = new CreateReturnRequest(
                    defaultSaleUuid, "DEFECTO", "Notas", "NOTA_CREDITO",
                    List.of(new ReturnItemRequest(productId, "1")), false
            );

            ReturnItemDomain returnItem = new ReturnItemDomain(
                    1L, productId, 1, "1 UN", new BigDecimal("3500.00"), new BigDecimal("3500.00"), 1L
            );
            ReturnDomain returnDomain = ReturnDomain.create(
                    100L, "DEFECTO", "Notas", "NOTA_CREDITO",
                    50L, employeePersonId, outletId, clientId, List.of(returnItem)
            );
            when(returnRepositoryPort.save(any(ReturnDomain.class))).thenReturn(returnDomain);

            ReturnResponse response = createReturnUseCase.execute(request);

            assertNotNull(response);
            verify(creditRepositoryPort, atLeastOnce()).save(any(CreditAccountDomain.class));

            ArgumentCaptor<CreditTransactionDomain> txnCaptor = ArgumentCaptor.forClass(CreditTransactionDomain.class);
            verify(creditRepositoryPort).save(txnCaptor.capture());
            assertEquals("RETURN_CREDIT", txnCaptor.getValue().getType());
        }

        @Test
        @DisplayName("Should process NOTA_CREDITO resolution when existing debt is zero without calling pay")
        void shouldProcessNotaCreditoWhenExistingDebtIsZero() {
            setupBaseMocks();
            CreditAccountDomain zeroDebtAccount = CreditAccountDomain.reconstruct(
                    300L, clientId, outletId, new BigDecimal("150000"),
                    BigDecimal.ZERO, 1L, LocalDateTime.now(), LocalDateTime.now()
            );
            when(creditRepositoryPort.findByClientIdAndOutletId(clientId, outletId))
                    .thenReturn(Optional.of(zeroDebtAccount));

            CreateReturnRequest request = new CreateReturnRequest(
                    defaultSaleUuid, "DEFECTO", "Notas", "NOTA_CREDITO",
                    List.of(new ReturnItemRequest(productId, "1")), false
            );

            ReturnItemDomain returnItem = new ReturnItemDomain(
                    1L, productId, 1, "1 UN", new BigDecimal("3500.00"), new BigDecimal("3500.00"), 1L
            );
            ReturnDomain returnDomain = ReturnDomain.create(
                    100L, "DEFECTO", "Notas", "NOTA_CREDITO",
                    50L, employeePersonId, outletId, clientId, List.of(returnItem)
            );
            when(returnRepositoryPort.save(any(ReturnDomain.class))).thenReturn(returnDomain);

            ReturnResponse response = createReturnUseCase.execute(request);

            assertNotNull(response);
            verify(creditRepositoryPort, never()).save(zeroDebtAccount); // Zero debt -> pay skipped
            verify(creditRepositoryPort, times(1)).save(any(CreditTransactionDomain.class));
        }

        @Test
        @DisplayName("Should process return with CAMBIO resolution successfully")
        void shouldProcessReturnWithCambioResolutionSuccessfully() {
            setupBaseMocks();
            CreateReturnRequest request = new CreateReturnRequest(
                    defaultSaleUuid, "INCORRECTO", "Cambio por otra talla", "CAMBIO",
                    List.of(new ReturnItemRequest(productId, "2")), false
            );

            ReturnItemDomain returnItem = new ReturnItemDomain(
                    1L, productId, 2, "2 UN", new BigDecimal("3500.00"), new BigDecimal("7000.00"), 1L
            );
            ReturnDomain returnDomain = ReturnDomain.create(
                    100L, "INCORRECTO", "Cambio por otra talla", "CAMBIO",
                    50L, employeePersonId, outletId, clientId, List.of(returnItem)
            );
            when(returnRepositoryPort.save(any(ReturnDomain.class))).thenReturn(returnDomain);

            ReturnResponse response = createReturnUseCase.execute(request);

            assertNotNull(response);
            assertEquals("CAMBIO", response.resolutionType());
            assertEquals("INCORRECTO", response.reason());
            verify(productOutletRepositoryPort, times(1)).save(any(ProductDomain.class));
            verify(returnRepositoryPort, times(1)).save(any(ReturnDomain.class));
        }
    }
}
