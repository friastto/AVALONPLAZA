package org.frias.avalon.domain.sale.application.usecase.sale.create;

import org.frias.avalon.core.exeptions.BusinessException;
import org.frias.avalon.core.exeptions.DomainValidationException;
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
import org.frias.avalon.domain.notification.application.event.SaleCreatedEvent;
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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para CreateSaleUseCaseImpl.
 * Cobertura de metodos de pago, reduccion de stock, validacion de cliente y ramas de excepcion.
 */
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
    @Mock private CreditRepositoryPort creditRepositoryPort;
    @Mock private ApplicationEventPublisher eventPublisher;

    @Mock private MasterTree masterTree;

    @InjectMocks
    private CreateSaleUseCaseImpl useCase;

    private static final Long OUTLET_ID = 4L;
    private static final Long EMPLOYEE_PERSON_ID = 20L;
    private static final Long CLIENT_PERSON_ID = 30L;
    private static final String CLIENT_NUMBER_ID = "12345678";
    private static final String USERNAME = "cashier_user";
    private static final Long ACTIVE_STATUS_ID = 5L;

    @BeforeEach
    void setUp() {
        lenient().when(masterTreeProvider.getTree()).thenReturn(masterTree);
    }

    // --- Metodos Auxiliares de Configuracion ---

    private UserContext createDefaultUserContext(List<String> roles) {
        return new UserContext(USERNAME, roles, OUTLET_ID);
    }

    private void setupSecurityDefaults(List<String> roles) {
        UserContext uc = createDefaultUserContext(roles);
        when(currentUserProvider.getCurrentUserContext()).thenReturn(uc);
        when(currentUserProvider.getCurrentOutletId()).thenReturn(OUTLET_ID);
        when(currentUserProvider.hasRole("ROLE_ADMIN")).thenReturn(false);
        when(currentUserProvider.hasRole("ROLE_ADMINTI")).thenReturn(false);
    }

    private void setupRoleAuthorization(String roleCode, boolean isChildOpt, boolean isChildGestion, boolean isChildAdminSys) {
        MasterRoot roleNode = new MasterRoot(100L, roleCode, "Role " + roleCode, 10L, 1L);
        when(masterTree.getByCode(roleCode)).thenReturn(roleNode);
        if (isChildOpt) when(masterTree.isChildOf(roleNode, "OPT")).thenReturn(true);
        else when(masterTree.isChildOf(roleNode, "OPT")).thenReturn(false);

        if (!isChildOpt && isChildGestion) when(masterTree.isChildOf(roleNode, "GESTION")).thenReturn(true);
        else if (!isChildOpt) when(masterTree.isChildOf(roleNode, "GESTION")).thenReturn(false);

        if (!isChildOpt && !isChildGestion && isChildAdminSys) when(masterTree.isChildOf(roleNode, "ADMINSYS")).thenReturn(true);
        else if (!isChildOpt && !isChildGestion) when(masterTree.isChildOf(roleNode, "ADMINSYS")).thenReturn(false);
    }

    private UserAvalonDomain createDefaultUser() {
        return UserAvalonDomain.fromPersistenceBasic(1L, EMPLOYEE_PERSON_ID, USERNAME, 1L);
    }

    private PersonDomain createDefaultClient() {
        return PersonDomain.createFromEntity(
                CLIENT_PERSON_ID, CLIENT_NUMBER_ID, "Juan", "Perez", "Calle 123",
                1L, 1L, 987654321L, "juan.perez@email.com", 1L, LocalDateTime.now(), LocalDateTime.now()
        );
    }

    private ProductDomain createDefaultProduct(Long productId, Long unitMeasureId, BigDecimal price, int initialStock) {
        return ProductDomain.fromPersistence(
                productId, "Producto Test", "Descripcion", initialStock, unitMeasureId, "img.jpg",
                price, OUTLET_ID, 1L, LocalDateTime.now(), LocalDateTime.now()
        );
    }

    private SaleDomain createSavedSale(Long saleId, Long paymentMethodId, BigDecimal totalAmount, BigDecimal amountReceived, BigDecimal changeGiven) {
        return SaleDomain.fromPersistence(
                saleId, UUID.randomUUID(), totalAmount, amountReceived, changeGiven,
                paymentMethodId, ACTIVE_STATUS_ID, CLIENT_PERSON_ID, OUTLET_ID, EMPLOYEE_PERSON_ID,
                LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), List.of()
        );
    }

    @Nested
    @DisplayName("Metodos de Pago y Casos de Exito")
    class PaymentMethodsAndSuccessCases {

        @Test
        @DisplayName("Metodo de Pago 1: Efectivo (EFE) - Venta exitosa con vuelto y envio de correo")
        void execute_CashPayment_Success() {
            setupSecurityDefaults(List.of("ROLE_CAJERO"));
            setupRoleAuthorization("CAJERO", true, false, false);

            when(userAvalonRepositoryPort.findByUserName(USERNAME)).thenReturn(Optional.of(createDefaultUser()));
            when(personRepositoryPort.findByNumberid(CLIENT_NUMBER_ID)).thenReturn(Optional.of(createDefaultClient()));
            when(masterDataRepositoryPort.getIdByCode("ACT")).thenReturn(ACTIVE_STATUS_ID);

            ProductDomain product = createDefaultProduct(50L, 11L, new BigDecimal("5.00"), 10000);
            when(productOutletRepositoryPort.findById(50L)).thenReturn(Optional.of(product));

            MasterRoot unitNode = new MasterRoot(11L, "KG", "Kilogramos", 200L, 1L);
            when(masterTree.getById(11L)).thenReturn(unitNode);
            when(weightConversionService.isWeighable("KG")).thenReturn(true);
            when(weightConversionService.convertToBaseUnit(new BigDecimal("1.500"), "KG")).thenReturn(1500);
            when(weightConversionService.formatFromBaseUnit(1500, "KG")).thenReturn("1.5 KG");

            MasterRoot payNode = new MasterRoot(8L, "EFE", "Efectivo", 300L, 1L);
            MasterRoot statusNode = new MasterRoot(ACTIVE_STATUS_ID, "ACT", "Activo", 400L, 1L);
            when(masterTree.getById(8L)).thenReturn(payNode);
            when(masterTree.getById(ACTIVE_STATUS_ID)).thenReturn(statusNode);

            SaleDomain savedSale = createSavedSale(100L, 8L, new BigDecimal("7.50"), new BigDecimal("10.00"), new BigDecimal("2.50"));
            when(saleRepositoryPort.save(any(SaleDomain.class))).thenReturn(savedSale);

            CreateSaleRequest request = new CreateSaleRequest(
                    CLIENT_NUMBER_ID, OUTLET_ID, 8L, new BigDecimal("10.00"),
                    List.of(new SaleItemRequest(50L, "1.500")),
                    true
            );

            SaleResponse response = useCase.execute(request);

            assertNotNull(response);
            assertEquals(100L, response.id());
            assertEquals(new BigDecimal("7.50"), response.totalAmount());
            assertEquals(new BigDecimal("2.50"), response.changeGiven());
            assertEquals("Juan Perez", response.clientFullName());

            assertEquals(8500, product.getStock());
            verify(productOutletRepositoryPort, times(1)).save(product);
            verify(saleRepositoryPort, times(1)).save(any(SaleDomain.class));

            ArgumentCaptor<SaleCreatedEvent> eventCaptor = ArgumentCaptor.forClass(SaleCreatedEvent.class);
            verify(eventPublisher, times(1)).publishEvent(eventCaptor.capture());
            assertEquals("juan.perez@email.com", eventCaptor.getValue().getClientEmail());
        }

        @Test
        @DisplayName("Metodo de Pago 2: Tarjeta (TAR) - Producto no pesable entero sin envio de correo")
        void execute_CardPayment_Success() {
            setupSecurityDefaults(List.of("ROLE_CAJERO"));
            setupRoleAuthorization("CAJERO", true, false, false);

            when(userAvalonRepositoryPort.findByUserName(USERNAME)).thenReturn(Optional.of(createDefaultUser()));
            when(personRepositoryPort.findByNumberid(CLIENT_NUMBER_ID)).thenReturn(Optional.of(createDefaultClient()));
            when(masterDataRepositoryPort.getIdByCode("ACT")).thenReturn(ACTIVE_STATUS_ID);

            ProductDomain product = createDefaultProduct(60L, 12L, new BigDecimal("10.00"), 50);
            when(productOutletRepositoryPort.findById(60L)).thenReturn(Optional.of(product));

            MasterRoot unitNode = new MasterRoot(12L, "UNI", "Unidades", 200L, 1L);
            when(masterTree.getById(12L)).thenReturn(unitNode);
            when(weightConversionService.isWeighable("UNI")).thenReturn(false);
            when(weightConversionService.formatFromBaseUnit(3, "UNI")).thenReturn("3 UNI");

            MasterRoot payNode = new MasterRoot(9L, "TAR", "Tarjeta", 300L, 1L);
            MasterRoot statusNode = new MasterRoot(ACTIVE_STATUS_ID, "ACT", "Activo", 400L, 1L);
            when(masterTree.getById(9L)).thenReturn(payNode);
            when(masterTree.getById(ACTIVE_STATUS_ID)).thenReturn(statusNode);

            SaleDomain savedSale = createSavedSale(101L, 9L, new BigDecimal("30.00"), new BigDecimal("30.00"), BigDecimal.ZERO);
            when(saleRepositoryPort.save(any(SaleDomain.class))).thenReturn(savedSale);

            CreateSaleRequest request = new CreateSaleRequest(
                    CLIENT_NUMBER_ID, OUTLET_ID, 9L, new BigDecimal("30.00"),
                    List.of(new SaleItemRequest(60L, "3")),
                    false
            );

            SaleResponse response = useCase.execute(request);

            assertNotNull(response);
            assertEquals(101L, response.id());
            assertEquals(new BigDecimal("30.00"), response.totalAmount());
            assertEquals(47, product.getStock());

            ArgumentCaptor<SaleCreatedEvent> eventCaptor = ArgumentCaptor.forClass(SaleCreatedEvent.class);
            verify(eventPublisher, times(1)).publishEvent(eventCaptor.capture());
            assertNull(eventCaptor.getValue().getClientEmail());
        }

        @Test
        @DisplayName("Metodo de Pago 3: Digital (DIG) - Producto pesable litros (L) sin monto recibido explícito")
        void execute_DigitalPayment_Success() {
            setupSecurityDefaults(List.of("ROLE_CAJERO"));
            setupRoleAuthorization("CAJERO", true, false, false);

            when(userAvalonRepositoryPort.findByUserName(USERNAME)).thenReturn(Optional.of(createDefaultUser()));
            when(personRepositoryPort.findByNumberid(CLIENT_NUMBER_ID)).thenReturn(Optional.of(createDefaultClient()));
            when(masterDataRepositoryPort.getIdByCode("ACT")).thenReturn(ACTIVE_STATUS_ID);

            ProductDomain product = createDefaultProduct(70L, 13L, new BigDecimal("8.00"), 50000);
            when(productOutletRepositoryPort.findById(70L)).thenReturn(Optional.of(product));

            MasterRoot unitNode = new MasterRoot(13L, "L", "Litros", 200L, 1L);
            when(masterTree.getById(13L)).thenReturn(unitNode);
            when(weightConversionService.isWeighable("L")).thenReturn(true);
            when(weightConversionService.convertToBaseUnit(new BigDecimal("2.5"), "L")).thenReturn(2500);
            when(weightConversionService.formatFromBaseUnit(2500, "L")).thenReturn("2.5 L");

            MasterRoot payNode = new MasterRoot(10L, "DIG", "Digital", 300L, 1L);
            MasterRoot statusNode = new MasterRoot(ACTIVE_STATUS_ID, "ACT", "Activo", 400L, 1L);
            when(masterTree.getById(10L)).thenReturn(payNode);
            when(masterTree.getById(ACTIVE_STATUS_ID)).thenReturn(statusNode);

            SaleDomain savedSale = createSavedSale(102L, 10L, new BigDecimal("20.00"), BigDecimal.ZERO, BigDecimal.ZERO);
            when(saleRepositoryPort.save(any(SaleDomain.class))).thenReturn(savedSale);

            CreateSaleRequest request = new CreateSaleRequest(
                    CLIENT_NUMBER_ID, OUTLET_ID, 10L, null,
                    List.of(new SaleItemRequest(70L, "2.5")),
                    null
            );

            SaleResponse response = useCase.execute(request);

            assertNotNull(response);
            assertEquals(102L, response.id());
            verify(creditRepositoryPort, never()).save(any(CreditAccountDomain.class));
        }

        @Test
        @DisplayName("Metodo de Pago 4: Fiado (FIA) - Cuenta de credito existente")
        void execute_CreditPayment_ExistingCreditAccount_Success() {
            setupSecurityDefaults(List.of("ROLE_CAJERO"));
            setupRoleAuthorization("CAJERO", true, false, false);

            when(userAvalonRepositoryPort.findByUserName(USERNAME)).thenReturn(Optional.of(createDefaultUser()));
            when(personRepositoryPort.findByNumberid(CLIENT_NUMBER_ID)).thenReturn(Optional.of(createDefaultClient()));
            when(masterDataRepositoryPort.getIdByCode("ACT")).thenReturn(ACTIVE_STATUS_ID);

            ProductDomain product = createDefaultProduct(50L, 12L, new BigDecimal("15.00"), 20);
            when(productOutletRepositoryPort.findById(50L)).thenReturn(Optional.of(product));

            MasterRoot unitNode = new MasterRoot(12L, "UNI", "Unidades", 200L, 1L);
            when(masterTree.getById(12L)).thenReturn(unitNode);
            when(weightConversionService.isWeighable("UNI")).thenReturn(false);
            when(weightConversionService.formatFromBaseUnit(2, "UNI")).thenReturn("2 UNI");

            MasterRoot payNode = new MasterRoot(15L, "FIA", "Fiado", 300L, 1L);
            MasterRoot statusNode = new MasterRoot(ACTIVE_STATUS_ID, "ACT", "Activo", 400L, 1L);
            when(masterTree.getById(15L)).thenReturn(payNode);
            when(masterTree.getById(ACTIVE_STATUS_ID)).thenReturn(statusNode);

            SaleDomain savedSale = createSavedSale(103L, 15L, new BigDecimal("30.00"), new BigDecimal("30.00"), BigDecimal.ZERO);
            when(saleRepositoryPort.save(any(SaleDomain.class))).thenReturn(savedSale);

            CreditAccountDomain existingAccount = CreditAccountDomain.reconstruct(
                    200L, CLIENT_PERSON_ID, OUTLET_ID, new BigDecimal("150000"),
                    new BigDecimal("50.00"), ACTIVE_STATUS_ID, LocalDateTime.now(), LocalDateTime.now()
            );
            when(creditRepositoryPort.findByClientIdAndOutletId(CLIENT_PERSON_ID, OUTLET_ID))
                    .thenReturn(Optional.of(existingAccount));

            CreateSaleRequest request = new CreateSaleRequest(
                    CLIENT_NUMBER_ID, OUTLET_ID, 15L, new BigDecimal("30.00"),
                    List.of(new SaleItemRequest(50L, "2")),
                    false
            );

            SaleResponse response = useCase.execute(request);

            assertNotNull(response);
            assertEquals(103L, response.id());
            assertEquals(new BigDecimal("80.00"), existingAccount.getCurrentDebt());
            verify(creditRepositoryPort, times(1)).save(existingAccount);
            verify(creditRepositoryPort, times(1)).save(any(CreditTransactionDomain.class));
        }

        @Test
        @DisplayName("Metodo de Pago 4: Fiado (FIA) - Cuenta de credito inexistente que es creada automaticamente")
        void execute_CreditPayment_NewCreditAccount_Success() {
            setupSecurityDefaults(List.of("ROLE_CAJERO"));
            setupRoleAuthorization("CAJERO", true, false, false);

            when(userAvalonRepositoryPort.findByUserName(USERNAME)).thenReturn(Optional.of(createDefaultUser()));
            when(personRepositoryPort.findByNumberid(CLIENT_NUMBER_ID)).thenReturn(Optional.of(createDefaultClient()));
            when(masterDataRepositoryPort.getIdByCode("ACT")).thenReturn(ACTIVE_STATUS_ID);

            ProductDomain product = createDefaultProduct(50L, 12L, new BigDecimal("25.00"), 20);
            when(productOutletRepositoryPort.findById(50L)).thenReturn(Optional.of(product));

            MasterRoot unitNode = new MasterRoot(12L, "UNI", "Unidades", 200L, 1L);
            when(masterTree.getById(12L)).thenReturn(unitNode);
            when(weightConversionService.isWeighable("UNI")).thenReturn(false);
            when(weightConversionService.formatFromBaseUnit(1, "UNI")).thenReturn("1 UNI");

            MasterRoot payNode = new MasterRoot(15L, "FIA", "Fiado", 300L, 1L);
            MasterRoot statusNode = new MasterRoot(ACTIVE_STATUS_ID, "ACT", "Activo", 400L, 1L);
            when(masterTree.getById(15L)).thenReturn(payNode);
            when(masterTree.getById(ACTIVE_STATUS_ID)).thenReturn(statusNode);

            SaleDomain savedSale = createSavedSale(104L, 15L, new BigDecimal("25.00"), new BigDecimal("25.00"), BigDecimal.ZERO);
            when(saleRepositoryPort.save(any(SaleDomain.class))).thenReturn(savedSale);

            when(creditRepositoryPort.findByClientIdAndOutletId(CLIENT_PERSON_ID, OUTLET_ID))
                    .thenReturn(Optional.empty());

            when(creditRepositoryPort.save(any(CreditAccountDomain.class))).thenAnswer(inv -> {
                CreditAccountDomain acc = inv.getArgument(0);
                if (acc.getId() == null) {
                    return CreditAccountDomain.reconstruct(
                            300L, acc.getClientId(), acc.getOutletId(), acc.getCreditLimit(),
                            acc.getCurrentDebt(), acc.getStatusId(), acc.getCreatedAt(), acc.getUpdatedAt()
                    );
                }
                return acc;
            });

            CreateSaleRequest request = new CreateSaleRequest(
                    CLIENT_NUMBER_ID, OUTLET_ID, 15L, new BigDecimal("25.00"),
                    List.of(new SaleItemRequest(50L, "1")),
                    false
            );

            SaleResponse response = useCase.execute(request);

            assertNotNull(response);
            assertEquals(104L, response.id());
            verify(creditRepositoryPort, times(2)).save(any(CreditAccountDomain.class));
            verify(creditRepositoryPort, times(1)).save(any(CreditTransactionDomain.class));
        }
    }

    @Nested
    @DisplayName("Unidades de Medida y Calculos de Subtotal")
    class UnitMeasuresAndCalculations {

        @Test
        @DisplayName("Producto pesable en Libras (LB) - Factor 453.59237 y cantidad con coma decimal")
        void execute_WeighableProduct_PoundsUnit_Success() {
            setupSecurityDefaults(List.of("ROLE_CAJERO"));
            setupRoleAuthorization("CAJERO", true, false, false);

            when(userAvalonRepositoryPort.findByUserName(USERNAME)).thenReturn(Optional.of(createDefaultUser()));
            when(personRepositoryPort.findByNumberid(CLIENT_NUMBER_ID)).thenReturn(Optional.of(createDefaultClient()));
            when(masterDataRepositoryPort.getIdByCode("ACT")).thenReturn(ACTIVE_STATUS_ID);

            ProductDomain product = createDefaultProduct(55L, 14L, new BigDecimal("10.00"), 100000);
            when(productOutletRepositoryPort.findById(55L)).thenReturn(Optional.of(product));

            MasterRoot unitNode = new MasterRoot(14L, "LB", "Libras", 200L, 1L);
            when(masterTree.getById(14L)).thenReturn(unitNode);
            when(weightConversionService.isWeighable("LB")).thenReturn(true);
            when(weightConversionService.convertToBaseUnit(new BigDecimal("2.5"), "LB")).thenReturn(1134);
            when(weightConversionService.formatFromBaseUnit(1134, "LB")).thenReturn("2.5 LB");

            MasterRoot payNode = new MasterRoot(8L, "EFE", "Efectivo", 300L, 1L);
            MasterRoot statusNode = new MasterRoot(ACTIVE_STATUS_ID, "ACT", "Activo", 400L, 1L);
            when(masterTree.getById(8L)).thenReturn(payNode);
            when(masterTree.getById(ACTIVE_STATUS_ID)).thenReturn(statusNode);

            SaleDomain savedSale = createSavedSale(105L, 8L, new BigDecimal("25.00"), new BigDecimal("30.00"), new BigDecimal("5.00"));
            when(saleRepositoryPort.save(any(SaleDomain.class))).thenReturn(savedSale);

            CreateSaleRequest request = new CreateSaleRequest(
                    CLIENT_NUMBER_ID, OUTLET_ID, 8L, new BigDecimal("30.00"),
                    List.of(new SaleItemRequest(55L, "2,5")),
                    false
            );

            SaleResponse response = useCase.execute(request);
            assertNotNull(response);
        }

        @Test
        @DisplayName("Producto pesable con unidad por defecto (GR) - Factor BigDecimal.ONE")
        void execute_WeighableProduct_DefaultUnit_Success() {
            setupSecurityDefaults(List.of("ROLE_CAJERO"));
            setupRoleAuthorization("CAJERO", true, false, false);

            when(userAvalonRepositoryPort.findByUserName(USERNAME)).thenReturn(Optional.of(createDefaultUser()));
            when(personRepositoryPort.findByNumberid(CLIENT_NUMBER_ID)).thenReturn(Optional.of(createDefaultClient()));
            when(masterDataRepositoryPort.getIdByCode("ACT")).thenReturn(ACTIVE_STATUS_ID);

            ProductDomain product = createDefaultProduct(56L, 15L, new BigDecimal("0.05"), 1000);
            when(productOutletRepositoryPort.findById(56L)).thenReturn(Optional.of(product));

            MasterRoot unitNode = new MasterRoot(15L, "GR", "Gramos", 200L, 1L);
            when(masterTree.getById(15L)).thenReturn(unitNode);
            when(weightConversionService.isWeighable("GR")).thenReturn(true);
            when(weightConversionService.convertToBaseUnit(new BigDecimal("100"), "GR")).thenReturn(100);
            when(weightConversionService.formatFromBaseUnit(100, "GR")).thenReturn("100 GR");

            MasterRoot payNode = new MasterRoot(8L, "EFE", "Efectivo", 300L, 1L);
            MasterRoot statusNode = new MasterRoot(ACTIVE_STATUS_ID, "ACT", "Activo", 400L, 1L);
            when(masterTree.getById(8L)).thenReturn(payNode);
            when(masterTree.getById(ACTIVE_STATUS_ID)).thenReturn(statusNode);

            SaleDomain savedSale = createSavedSale(106L, 8L, new BigDecimal("5.00"), new BigDecimal("5.00"), BigDecimal.ZERO);
            when(saleRepositoryPort.save(any(SaleDomain.class))).thenReturn(savedSale);

            CreateSaleRequest request = new CreateSaleRequest(
                    CLIENT_NUMBER_ID, OUTLET_ID, 8L, new BigDecimal("5.00"),
                    List.of(new SaleItemRequest(56L, "100")),
                    false
            );

            SaleResponse response = useCase.execute(request);
            assertNotNull(response);
        }

        @Test
        @DisplayName("Producto con customLineTotal explicito que invalida calculo estandar")
        void execute_ProductWithCustomLineTotal_Success() {
            setupSecurityDefaults(List.of("ROLE_CAJERO"));
            setupRoleAuthorization("CAJERO", true, false, false);

            when(userAvalonRepositoryPort.findByUserName(USERNAME)).thenReturn(Optional.of(createDefaultUser()));
            when(personRepositoryPort.findByNumberid(CLIENT_NUMBER_ID)).thenReturn(Optional.of(createDefaultClient()));
            when(masterDataRepositoryPort.getIdByCode("ACT")).thenReturn(ACTIVE_STATUS_ID);

            ProductDomain product = createDefaultProduct(57L, 12L, new BigDecimal("10.00"), 50);
            when(productOutletRepositoryPort.findById(57L)).thenReturn(Optional.of(product));

            MasterRoot unitNode = new MasterRoot(12L, "UNI", "Unidades", 200L, 1L);
            when(masterTree.getById(12L)).thenReturn(unitNode);
            when(weightConversionService.isWeighable("UNI")).thenReturn(false);
            when(weightConversionService.formatFromBaseUnit(2, "UNI")).thenReturn("2 UNI");

            MasterRoot payNode = new MasterRoot(8L, "EFE", "Efectivo", 300L, 1L);
            MasterRoot statusNode = new MasterRoot(ACTIVE_STATUS_ID, "ACT", "Activo", 400L, 1L);
            when(masterTree.getById(8L)).thenReturn(payNode);
            when(masterTree.getById(ACTIVE_STATUS_ID)).thenReturn(statusNode);

            SaleDomain savedSale = createSavedSale(107L, 8L, new BigDecimal("15.00"), new BigDecimal("20.00"), new BigDecimal("5.00"));
            when(saleRepositoryPort.save(any(SaleDomain.class))).thenReturn(savedSale);

            CreateSaleRequest request = new CreateSaleRequest(
                    CLIENT_NUMBER_ID, OUTLET_ID, 8L, new BigDecimal("20.00"),
                    List.of(new SaleItemRequest(57L, "2", new BigDecimal("15.00"))),
                    false
            );

            SaleResponse response = useCase.execute(request);
            assertNotNull(response);
        }
    }

    @Nested
    @DisplayName("Seguridad, Permisos e Aislamiento de Tienda")
    class SecurityAndPermissions {

        @Test
        @DisplayName("Usuario ROLE_ADMIN ignora aislamiento de tienda y verificacion de rol operativo")
        void execute_SystemAdminRole_BypassesTenantAndRoleChecks() {
            UserContext uc = createDefaultUserContext(List.of("ROLE_ADMIN"));
            when(currentUserProvider.getCurrentUserContext()).thenReturn(uc);
            when(currentUserProvider.hasRole("ROLE_ADMIN")).thenReturn(true);

            when(userAvalonRepositoryPort.findByUserName(USERNAME)).thenReturn(Optional.of(createDefaultUser()));
            when(personRepositoryPort.findByNumberid(CLIENT_NUMBER_ID)).thenReturn(Optional.of(createDefaultClient()));
            when(masterDataRepositoryPort.getIdByCode("ACT")).thenReturn(ACTIVE_STATUS_ID);

            ProductDomain product = ProductDomain.fromPersistence(
                    50L, "Producto Test", "Descripcion", 10, 12L, "img.jpg",
                    new BigDecimal("10.00"), 99L, 1L, LocalDateTime.now(), LocalDateTime.now()
            );
            when(productOutletRepositoryPort.findById(50L)).thenReturn(Optional.of(product));

            MasterRoot unitNode = new MasterRoot(12L, "UNI", "Unidades", 200L, 1L);
            when(masterTree.getById(12L)).thenReturn(unitNode);
            when(weightConversionService.isWeighable("UNI")).thenReturn(false);
            when(weightConversionService.formatFromBaseUnit(1, "UNI")).thenReturn("1 UNI");

            MasterRoot payNode = new MasterRoot(8L, "EFE", "Efectivo", 300L, 1L);
            MasterRoot statusNode = new MasterRoot(ACTIVE_STATUS_ID, "ACT", "Activo", 400L, 1L);
            when(masterTree.getById(8L)).thenReturn(payNode);
            when(masterTree.getById(ACTIVE_STATUS_ID)).thenReturn(statusNode);

            SaleDomain savedSale = createSavedSale(109L, 8L, new BigDecimal("10.00"), new BigDecimal("10.00"), BigDecimal.ZERO);
            when(saleRepositoryPort.save(any(SaleDomain.class))).thenReturn(savedSale);

            CreateSaleRequest request = new CreateSaleRequest(
                    CLIENT_NUMBER_ID, 99L, 8L, new BigDecimal("10.00"),
                    List.of(new SaleItemRequest(50L, "1")),
                    false
            );

            SaleResponse response = useCase.execute(request);
            assertNotNull(response);
        }

        @Test
        @DisplayName("Sin admin y tienda nula en contexto -> Lanza BusinessException")
        void execute_TenantOutletNull_ThrowsBusinessException() {
            UserContext uc = createDefaultUserContext(List.of("ROLE_CAJERO"));
            when(currentUserProvider.getCurrentUserContext()).thenReturn(uc);
            when(currentUserProvider.hasRole("ROLE_ADMIN")).thenReturn(false);
            when(currentUserProvider.hasRole("ROLE_ADMINTI")).thenReturn(false);
            when(currentUserProvider.getCurrentOutletId()).thenReturn(null);

            CreateSaleRequest request = new CreateSaleRequest(
                    CLIENT_NUMBER_ID, OUTLET_ID, 8L, new BigDecimal("10.00"),
                    List.of(new SaleItemRequest(50L, "1")), false
            );

            BusinessException ex = assertThrows(BusinessException.class, () -> useCase.execute(request));
            assertTrue(ex.getMessage().contains("No se detectó una tienda asociada"));
        }

        @Test
        @DisplayName("Sin admin y tienda de solicitud diferente a tienda en contexto -> Lanza BusinessException")
        void execute_TenantOutletMismatch_ThrowsBusinessException() {
            setupSecurityDefaults(List.of("ROLE_CAJERO"));

            CreateSaleRequest request = new CreateSaleRequest(
                    CLIENT_NUMBER_ID, 99L, 8L, new BigDecimal("10.00"),
                    List.of(new SaleItemRequest(50L, "1")), false
            );

            BusinessException ex = assertThrows(BusinessException.class, () -> useCase.execute(request));
            assertTrue(ex.getMessage().contains("No tienes permisos para registrar ventas en otra tienda"));
        }

        @Test
        @DisplayName("Rol no autorizado (ROLE_GUEST) -> Lanza BusinessException")
        void execute_UnauthorizedRole_ThrowsBusinessException() {
            setupSecurityDefaults(List.of("ROLE_GUEST"));

            MasterRoot roleNode = new MasterRoot(200L, "GUEST", "Invitado", 10L, 1L);
            when(masterTree.getByCode("GUEST")).thenReturn(roleNode);
            when(masterTree.isChildOf(roleNode, "OPT")).thenReturn(false);
            when(masterTree.isChildOf(roleNode, "GESTION")).thenReturn(false);
            when(masterTree.isChildOf(roleNode, "ADMINSYS")).thenReturn(false);

            CreateSaleRequest request = new CreateSaleRequest(
                    CLIENT_NUMBER_ID, OUTLET_ID, 8L, new BigDecimal("10.00"),
                    List.of(new SaleItemRequest(50L, "1")), false
            );

            BusinessException ex = assertThrows(BusinessException.class, () -> useCase.execute(request));
            assertTrue(ex.getMessage().contains("Tu rol actual no tiene autorización para registrar ventas"));
        }

        @Test
        @DisplayName("Rol autorizado por ser hijo de GESTION")
        void execute_AuthorizedRole_ChildOfGestion_Success() {
            setupSecurityDefaults(List.of("ROLE_MGR"));
            setupRoleAuthorization("MGR", false, true, false);
            setupCommonSuccessMocks();

            CreateSaleRequest request = new CreateSaleRequest(
                    CLIENT_NUMBER_ID, OUTLET_ID, 8L, new BigDecimal("10.00"),
                    List.of(new SaleItemRequest(50L, "1")), false
            );

            assertNotNull(useCase.execute(request));
        }

        @Test
        @DisplayName("Rol autorizado por ser hijo de ADMINSYS")
        void execute_AuthorizedRole_ChildOfAdminSys_Success() {
            setupSecurityDefaults(List.of("ROLE_SYSOP"));
            setupRoleAuthorization("SYSOP", false, false, true);
            setupCommonSuccessMocks();

            CreateSaleRequest request = new CreateSaleRequest(
                    CLIENT_NUMBER_ID, OUTLET_ID, 8L, new BigDecimal("10.00"),
                    List.of(new SaleItemRequest(50L, "1")), false
            );

            assertNotNull(useCase.execute(request));
        }

        @Test
        @DisplayName("Rol autorizado por coincidir codigo con OPERACION, GESTION, ADMINSYS, ADMIN, DUENO")
        void execute_AuthorizedRole_DirectCodes_Success() {
            setupSecurityDefaults(List.of("OPERACION"));
            MasterRoot roleNode = new MasterRoot(201L, "OPERACION", "Operacion", 10L, 1L);
            when(masterTree.getByCode("OPERACION")).thenReturn(roleNode);
            when(masterTree.isChildOf(roleNode, "OPT")).thenReturn(false);
            when(masterTree.isChildOf(roleNode, "GESTION")).thenReturn(false);
            when(masterTree.isChildOf(roleNode, "ADMINSYS")).thenReturn(false);

            setupCommonSuccessMocks();

            CreateSaleRequest request = new CreateSaleRequest(
                    CLIENT_NUMBER_ID, OUTLET_ID, 8L, new BigDecimal("10.00"),
                    List.of(new SaleItemRequest(50L, "1")), false
            );

            assertNotNull(useCase.execute(request));
        }

        @Test
        @DisplayName("Primer rol nulo en MasterData pero segundo rol es valido y autorizado")
        void execute_FirstRoleNullMasterNode_SecondRoleAuthorized_Success() {
            setupSecurityDefaults(List.of("ROLE_UNKNOWN", "ROLE_DUENO"));
            when(masterTree.getByCode("UNKNOWN")).thenReturn(null);

            MasterRoot duenoNode = new MasterRoot(202L, "DUENO", "Dueno", 10L, 1L);
            when(masterTree.getByCode("DUENO")).thenReturn(duenoNode);
            when(masterTree.isChildOf(duenoNode, "OPT")).thenReturn(false);
            when(masterTree.isChildOf(duenoNode, "GESTION")).thenReturn(false);
            when(masterTree.isChildOf(duenoNode, "ADMINSYS")).thenReturn(false);

            setupCommonSuccessMocks();

            CreateSaleRequest request = new CreateSaleRequest(
                    CLIENT_NUMBER_ID, OUTLET_ID, 8L, new BigDecimal("10.00"),
                    List.of(new SaleItemRequest(50L, "1")), false
            );

            assertNotNull(useCase.execute(request));
        }

        private void setupCommonSuccessMocks() {
            when(userAvalonRepositoryPort.findByUserName(USERNAME)).thenReturn(Optional.of(createDefaultUser()));
            when(personRepositoryPort.findByNumberid(CLIENT_NUMBER_ID)).thenReturn(Optional.of(createDefaultClient()));
            when(masterDataRepositoryPort.getIdByCode("ACT")).thenReturn(ACTIVE_STATUS_ID);

            ProductDomain product = createDefaultProduct(50L, 12L, new BigDecimal("10.00"), 10);
            when(productOutletRepositoryPort.findById(50L)).thenReturn(Optional.of(product));

            MasterRoot unitNode = new MasterRoot(12L, "UNI", "Unidades", 200L, 1L);
            when(masterTree.getById(12L)).thenReturn(unitNode);
            when(weightConversionService.isWeighable("UNI")).thenReturn(false);
            when(weightConversionService.formatFromBaseUnit(1, "UNI")).thenReturn("1 UNI");

            MasterRoot payNode = new MasterRoot(8L, "EFE", "Efectivo", 300L, 1L);
            MasterRoot statusNode = new MasterRoot(ACTIVE_STATUS_ID, "ACT", "Activo", 400L, 1L);
            when(masterTree.getById(8L)).thenReturn(payNode);
            when(masterTree.getById(ACTIVE_STATUS_ID)).thenReturn(statusNode);

            SaleDomain savedSale = createSavedSale(110L, 8L, new BigDecimal("10.00"), new BigDecimal("10.00"), BigDecimal.ZERO);
            when(saleRepositoryPort.save(any(SaleDomain.class))).thenReturn(savedSale);
        }
    }

    @Nested
    @DisplayName("Ramas de Excepcion y Validaciones")
    class ExceptionAndValidationBranches {

        @Test
        @DisplayName("Usuario autenticado no existe en el sistema -> Lanza ResourceNotFoundException")
        void execute_EmployeeUserNotFound_ThrowsResourceNotFoundException() {
            setupSecurityDefaults(List.of("ROLE_CAJERO"));
            setupRoleAuthorization("CAJERO", true, false, false);

            when(userAvalonRepositoryPort.findByUserName(USERNAME)).thenReturn(Optional.empty());

            CreateSaleRequest request = new CreateSaleRequest(
                    CLIENT_NUMBER_ID, OUTLET_ID, 8L, new BigDecimal("10.00"),
                    List.of(new SaleItemRequest(50L, "1")), false
            );

            ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () -> useCase.execute(request));
            assertTrue(ex.getMessage().contains("El usuario autenticado no existe en el sistema"));
        }

        @Test
        @DisplayName("Usuario autenticado sin persona (empleado) asociada -> Lanza BusinessException")
        void execute_EmployeePersonIdNull_ThrowsBusinessException() {
            setupSecurityDefaults(List.of("ROLE_CAJERO"));
            setupRoleAuthorization("CAJERO", true, false, false);

            UserAvalonDomain userWithoutPerson = UserAvalonDomain.fromPersistenceBasic(1L, null, USERNAME, 1L);
            when(userAvalonRepositoryPort.findByUserName(USERNAME)).thenReturn(Optional.of(userWithoutPerson));

            CreateSaleRequest request = new CreateSaleRequest(
                    CLIENT_NUMBER_ID, OUTLET_ID, 8L, new BigDecimal("10.00"),
                    List.of(new SaleItemRequest(50L, "1")), false
            );

            BusinessException ex = assertThrows(BusinessException.class, () -> useCase.execute(request));
            assertTrue(ex.getMessage().contains("registro de persona (empleado) asociado"));
        }

        @Test
        @DisplayName("Cliente con identificacion no encontrado -> Lanza ResourceNotFoundException")
        void execute_ClientNotFound_ThrowsResourceNotFoundException() {
            setupSecurityDefaults(List.of("ROLE_CAJERO"));
            setupRoleAuthorization("CAJERO", true, false, false);

            when(userAvalonRepositoryPort.findByUserName(USERNAME)).thenReturn(Optional.of(createDefaultUser()));
            when(personRepositoryPort.findByNumberid(CLIENT_NUMBER_ID)).thenReturn(Optional.empty());

            CreateSaleRequest request = new CreateSaleRequest(
                    CLIENT_NUMBER_ID, OUTLET_ID, 8L, new BigDecimal("10.00"),
                    List.of(new SaleItemRequest(50L, "1")), false
            );

            ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () -> useCase.execute(request));
            assertTrue(ex.getMessage().contains("Cliente con identificación"));
        }

        @Test
        @DisplayName("Estado Activo ('ACT') no encontrado en MasterData -> Lanza IllegalStateException")
        void execute_ActiveStatusNotFound_ThrowsIllegalStateException() {
            setupSecurityDefaults(List.of("ROLE_CAJERO"));
            setupRoleAuthorization("CAJERO", true, false, false);

            when(userAvalonRepositoryPort.findByUserName(USERNAME)).thenReturn(Optional.of(createDefaultUser()));
            when(personRepositoryPort.findByNumberid(CLIENT_NUMBER_ID)).thenReturn(Optional.of(createDefaultClient()));
            when(masterDataRepositoryPort.getIdByCode("ACT")).thenReturn(null);

            CreateSaleRequest request = new CreateSaleRequest(
                    CLIENT_NUMBER_ID, OUTLET_ID, 8L, new BigDecimal("10.00"),
                    List.of(new SaleItemRequest(50L, "1")), false
            );

            IllegalStateException ex = assertThrows(IllegalStateException.class, () -> useCase.execute(request));
            assertTrue(ex.getMessage().contains("Estado Activo ('ACT') no encontrado"));
        }

        @Test
        @DisplayName("Producto con ID no existe -> Lanza ResourceNotFoundException")
        void execute_ProductNotFound_ThrowsResourceNotFoundException() {
            setupSecurityDefaults(List.of("ROLE_CAJERO"));
            setupRoleAuthorization("CAJERO", true, false, false);

            when(userAvalonRepositoryPort.findByUserName(USERNAME)).thenReturn(Optional.of(createDefaultUser()));
            when(personRepositoryPort.findByNumberid(CLIENT_NUMBER_ID)).thenReturn(Optional.of(createDefaultClient()));
            when(masterDataRepositoryPort.getIdByCode("ACT")).thenReturn(ACTIVE_STATUS_ID);

            when(productOutletRepositoryPort.findById(50L)).thenReturn(Optional.empty());

            CreateSaleRequest request = new CreateSaleRequest(
                    CLIENT_NUMBER_ID, OUTLET_ID, 8L, new BigDecimal("10.00"),
                    List.of(new SaleItemRequest(50L, "1")), false
            );

            ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () -> useCase.execute(request));
            assertTrue(ex.getMessage().contains("El producto con ID 50 no existe"));
        }

        @Test
        @DisplayName("Producto no pertenece a la tienda de la venta -> Lanza BusinessException")
        void execute_ProductOutletMismatch_ThrowsBusinessException() {
            setupSecurityDefaults(List.of("ROLE_CAJERO"));
            setupRoleAuthorization("CAJERO", true, false, false);

            when(userAvalonRepositoryPort.findByUserName(USERNAME)).thenReturn(Optional.of(createDefaultUser()));
            when(personRepositoryPort.findByNumberid(CLIENT_NUMBER_ID)).thenReturn(Optional.of(createDefaultClient()));
            when(masterDataRepositoryPort.getIdByCode("ACT")).thenReturn(ACTIVE_STATUS_ID);

            ProductDomain productOtherOutlet = ProductDomain.fromPersistence(
                    50L, "Producto Otro Outlet", "Desc", 10, 12L, "img.jpg",
                    new BigDecimal("10.00"), 99L, 1L, LocalDateTime.now(), LocalDateTime.now()
            );
            when(productOutletRepositoryPort.findById(50L)).thenReturn(Optional.of(productOtherOutlet));

            CreateSaleRequest request = new CreateSaleRequest(
                    CLIENT_NUMBER_ID, OUTLET_ID, 8L, new BigDecimal("10.00"),
                    List.of(new SaleItemRequest(50L, "1")), false
            );

            BusinessException ex = assertThrows(BusinessException.class, () -> useCase.execute(request));
            assertTrue(ex.getMessage().contains("no pertenece a la tienda de la venta"));
        }

        @Test
        @DisplayName("Unidad de medida del producto invalida (no encontrada) -> Lanza DomainValidationException")
        void execute_UnitMeasureNotFound_ThrowsDomainValidationException() {
            setupSecurityDefaults(List.of("ROLE_CAJERO"));
            setupRoleAuthorization("CAJERO", true, false, false);

            when(userAvalonRepositoryPort.findByUserName(USERNAME)).thenReturn(Optional.of(createDefaultUser()));
            when(personRepositoryPort.findByNumberid(CLIENT_NUMBER_ID)).thenReturn(Optional.of(createDefaultClient()));
            when(masterDataRepositoryPort.getIdByCode("ACT")).thenReturn(ACTIVE_STATUS_ID);

            ProductDomain product = createDefaultProduct(50L, 999L, new BigDecimal("10.00"), 10);
            when(productOutletRepositoryPort.findById(50L)).thenReturn(Optional.of(product));
            when(masterTree.getById(999L)).thenReturn(null);

            CreateSaleRequest request = new CreateSaleRequest(
                    CLIENT_NUMBER_ID, OUTLET_ID, 8L, new BigDecimal("10.00"),
                    List.of(new SaleItemRequest(50L, "1")), false
            );

            DomainValidationException ex = assertThrows(DomainValidationException.class, () -> useCase.execute(request));
            assertTrue(ex.getMessage().contains("no es válida"));
        }

        @Test
        @DisplayName("Cantidad invalida decimal para producto pesable -> Lanza BusinessException")
        void execute_WeighableInvalidQuantityFormat_ThrowsBusinessException() {
            setupSecurityDefaults(List.of("ROLE_CAJERO"));
            setupRoleAuthorization("CAJERO", true, false, false);

            when(userAvalonRepositoryPort.findByUserName(USERNAME)).thenReturn(Optional.of(createDefaultUser()));
            when(personRepositoryPort.findByNumberid(CLIENT_NUMBER_ID)).thenReturn(Optional.of(createDefaultClient()));
            when(masterDataRepositoryPort.getIdByCode("ACT")).thenReturn(ACTIVE_STATUS_ID);

            ProductDomain product = createDefaultProduct(50L, 11L, new BigDecimal("10.00"), 1000);
            when(productOutletRepositoryPort.findById(50L)).thenReturn(Optional.of(product));

            MasterRoot unitNode = new MasterRoot(11L, "KG", "Kilogramos", 200L, 1L);
            when(masterTree.getById(11L)).thenReturn(unitNode);
            when(weightConversionService.isWeighable("KG")).thenReturn(true);

            CreateSaleRequest request = new CreateSaleRequest(
                    CLIENT_NUMBER_ID, OUTLET_ID, 8L, new BigDecimal("10.00"),
                    List.of(new SaleItemRequest(50L, "abc_invalid")), false
            );

            BusinessException ex = assertThrows(BusinessException.class, () -> useCase.execute(request));
            assertTrue(ex.getMessage().contains("no es un decimal válido"));
        }

        @Test
        @DisplayName("Cantidad invalida entero para producto no pesable -> Lanza BusinessException")
        void execute_NonWeighableInvalidQuantityFormat_ThrowsBusinessException() {
            setupSecurityDefaults(List.of("ROLE_CAJERO"));
            setupRoleAuthorization("CAJERO", true, false, false);

            when(userAvalonRepositoryPort.findByUserName(USERNAME)).thenReturn(Optional.of(createDefaultUser()));
            when(personRepositoryPort.findByNumberid(CLIENT_NUMBER_ID)).thenReturn(Optional.of(createDefaultClient()));
            when(masterDataRepositoryPort.getIdByCode("ACT")).thenReturn(ACTIVE_STATUS_ID);

            ProductDomain product = createDefaultProduct(50L, 12L, new BigDecimal("10.00"), 1000);
            when(productOutletRepositoryPort.findById(50L)).thenReturn(Optional.of(product));

            MasterRoot unitNode = new MasterRoot(12L, "UNI", "Unidades", 200L, 1L);
            when(masterTree.getById(12L)).thenReturn(unitNode);
            when(weightConversionService.isWeighable("UNI")).thenReturn(false);

            CreateSaleRequest request = new CreateSaleRequest(
                    CLIENT_NUMBER_ID, OUTLET_ID, 8L, new BigDecimal("10.00"),
                    List.of(new SaleItemRequest(50L, "1.5")), false
            );

            BusinessException ex = assertThrows(BusinessException.class, () -> useCase.execute(request));
            assertTrue(ex.getMessage().contains("debe ser un entero"));
        }

        @Test
        @DisplayName("Cantidad menor o igual a cero -> Lanza BusinessException")
        void execute_QuantityZeroOrNegative_ThrowsBusinessException() {
            setupSecurityDefaults(List.of("ROLE_CAJERO"));
            setupRoleAuthorization("CAJERO", true, false, false);

            when(userAvalonRepositoryPort.findByUserName(USERNAME)).thenReturn(Optional.of(createDefaultUser()));
            when(personRepositoryPort.findByNumberid(CLIENT_NUMBER_ID)).thenReturn(Optional.of(createDefaultClient()));
            when(masterDataRepositoryPort.getIdByCode("ACT")).thenReturn(ACTIVE_STATUS_ID);

            ProductDomain product = createDefaultProduct(50L, 12L, new BigDecimal("10.00"), 1000);
            when(productOutletRepositoryPort.findById(50L)).thenReturn(Optional.of(product));

            MasterRoot unitNode = new MasterRoot(12L, "UNI", "Unidades", 200L, 1L);
            when(masterTree.getById(12L)).thenReturn(unitNode);
            when(weightConversionService.isWeighable("UNI")).thenReturn(false);

            CreateSaleRequest request = new CreateSaleRequest(
                    CLIENT_NUMBER_ID, OUTLET_ID, 8L, new BigDecimal("10.00"),
                    List.of(new SaleItemRequest(50L, "0")), false
            );

            BusinessException ex = assertThrows(BusinessException.class, () -> useCase.execute(request));
            assertTrue(ex.getMessage().contains("debe ser mayor a cero"));
        }
    }
}
