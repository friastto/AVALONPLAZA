package org.frias.avalon.domain.sale.application.usecase.order.invoice;

import org.frias.avalon.core.exeptions.BusinessException;
import org.frias.avalon.core.exeptions.DomainValidationException;
import org.frias.avalon.core.exeptions.ResourceNotFoundException;
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
import org.frias.avalon.domain.sale.application.dto.response.SaleResponse;
import org.frias.avalon.domain.sale.application.port.OrderRepositoryPort;
import org.frias.avalon.domain.sale.application.port.SaleRepositoryPort;
import org.frias.avalon.domain.sale.domain.OrderDomain;
import org.frias.avalon.domain.sale.domain.OrderItemDomain;
import org.frias.avalon.domain.sale.domain.SaleDomain;
import org.frias.avalon.domain.user.domain.model.UserAvalonDomain;
import org.frias.avalon.domain.user.domain.port.UserAvalonRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvoiceOrderUseCaseImplTest {

    @Mock private OrderRepositoryPort orderRepositoryPort;
    @Mock private SaleRepositoryPort saleRepositoryPort;
    @Mock private ProductOutletRepositoryPort productOutletRepositoryPort;
    @Mock private PersonRepositoryPort personRepositoryPort;
    @Mock private UserAvalonRepositoryPort userAvalonRepositoryPort;
    @Mock private MasterDataRepositoryPort masterDataRepositoryPort;
    @Mock private MasterTreeProvider masterTreeProvider;
    @Mock private CurrentUserProviderPort currentUserProvider;

    @InjectMocks
    private InvoiceOrderUseCaseImpl useCase;

    private UserContext userContext;
    private MasterTree masterTree;

    @BeforeEach
    void setUp() {
        userContext = new UserContext("employee_user", List.of("ROLE_CAJTUR"), 4L);
        masterTree = mock(MasterTree.class);
    }

    @Test
    @DisplayName("Facturacion exitosa de pedido omnicanal con monto recibido explicito y calculo de cambio")
    void testInvoiceOrderSuccessfully_WithExplicitAmountReceived() {
        UUID orderCode = UUID.randomUUID();

        OrderItemDomain orderItem = new OrderItemDomain(1L, 50L, 2, "2 UND", new BigDecimal("100"), new BigDecimal("200"), 11L);
        OrderDomain order = OrderDomain.fromPersistence(
                5L, orderCode, new BigDecimal("200"), 8L, 100L, 4L,
                LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), List.of(orderItem)
        );
        when(orderRepositoryPort.findByCode(orderCode)).thenReturn(Optional.of(order));

        when(currentUserProvider.getCurrentUserContext()).thenReturn(userContext);
        when(currentUserProvider.getCurrentOutletId()).thenReturn(4L);
        when(currentUserProvider.hasRole(anyString())).thenReturn(false);

        when(masterTreeProvider.getTree()).thenReturn(masterTree);
        MasterRoot statusNodePEN = new MasterRoot(100L, "PEN", "Pendiente", 1000L, 1L);
        when(masterTree.getById(100L)).thenReturn(statusNodePEN);

        UserAvalonDomain user = UserAvalonDomain.fromPersistenceBasic(1L, 20L, "employee_user", 1L);
        when(userAvalonRepositoryPort.findByUserName("employee_user")).thenReturn(Optional.of(user));

        PersonDomain client = PersonDomain.createFromEntity(
                30L, "123456", "Juan", "Perez", "Calle 1",
                1L, 1L, 123456L, "juan@email.com", 1L, LocalDateTime.now(), LocalDateTime.now()
        );
        when(personRepositoryPort.findByNumberid("123456")).thenReturn(Optional.of(client));

        when(masterDataRepositoryPort.getIdByCode("ACT")).thenReturn(10L);
        when(masterDataRepositoryPort.getIdByCode("COM")).thenReturn(101L);

        ProductDomain product = ProductDomain.fromPersistence(
                50L, "Arroz", "desc", 100, 11L, "img",
                new BigDecimal("100"), 4L, 1L, LocalDateTime.now(), LocalDateTime.now()
        );
        when(productOutletRepositoryPort.findById(50L)).thenReturn(Optional.of(product));

        SaleDomain mockSavedSale = SaleDomain.fromPersistence(
                200L, UUID.randomUUID(), new BigDecimal("200"), new BigDecimal("250"), new BigDecimal("50"),
                8L, 10L, 30L, 4L, 20L, LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), List.of()
        );
        when(saleRepositoryPort.save(any(SaleDomain.class))).thenReturn(mockSavedSale);

        MasterRoot payNode = new MasterRoot(8L, "EFE", "Efectivo", 300L, 1L);
        MasterRoot statusNodeACT = new MasterRoot(10L, "ACT", "Activo", 400L, 1L);
        when(masterTree.getById(8L)).thenReturn(payNode);
        when(masterTree.getById(10L)).thenReturn(statusNodeACT);

        SaleResponse response = useCase.execute(orderCode, "123456", new BigDecimal("250"));

        assertNotNull(response);
        assertEquals(200L, response.id());
        assertEquals(new BigDecimal("200"), response.totalAmount());
        assertEquals(new BigDecimal("250"), response.amountReceived());
        assertEquals(new BigDecimal("50"), response.changeGiven());
        assertEquals("Juan Perez", response.clientFullName());

        assertEquals(98, product.getStock());
        verify(productOutletRepositoryPort, times(1)).save(product);
        verify(saleRepositoryPort, times(1)).save(any(SaleDomain.class));
        verify(orderRepositoryPort, times(1)).save(order);
        assertEquals(101L, order.getStatusId());
    }

    @Test
    @DisplayName("Facturacion exitosa cuando monto recibido es nulo (fallback a pago exacto)")
    void testInvoiceOrderSuccessfully_WithNullAmountReceived_ExactPaymentFallback() {
        UUID orderCode = UUID.randomUUID();

        OrderItemDomain orderItem = new OrderItemDomain(1L, 50L, 3, "3 UND", new BigDecimal("100"), new BigDecimal("300"), 11L);
        OrderDomain order = OrderDomain.fromPersistence(
                5L, orderCode, new BigDecimal("300"), 8L, 100L, 4L,
                LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), List.of(orderItem)
        );
        when(orderRepositoryPort.findByCode(orderCode)).thenReturn(Optional.of(order));

        when(currentUserProvider.getCurrentUserContext()).thenReturn(userContext);
        when(currentUserProvider.getCurrentOutletId()).thenReturn(4L);
        when(currentUserProvider.hasRole(anyString())).thenReturn(false);

        when(masterTreeProvider.getTree()).thenReturn(masterTree);
        MasterRoot statusNodePEN = new MasterRoot(100L, "PEN", "Pendiente", 1000L, 1L);
        when(masterTree.getById(100L)).thenReturn(statusNodePEN);

        UserAvalonDomain user = UserAvalonDomain.fromPersistenceBasic(1L, 20L, "employee_user", 1L);
        when(userAvalonRepositoryPort.findByUserName("employee_user")).thenReturn(Optional.of(user));

        PersonDomain client = PersonDomain.createFromEntity(
                30L, "123456", "Juan", "Perez", "Calle 1",
                1L, 1L, 123456L, "juan@email.com", 1L, LocalDateTime.now(), LocalDateTime.now()
        );
        when(personRepositoryPort.findByNumberid("123456")).thenReturn(Optional.of(client));

        when(masterDataRepositoryPort.getIdByCode("ACT")).thenReturn(10L);
        when(masterDataRepositoryPort.getIdByCode("COM")).thenReturn(101L);

        ProductDomain product = ProductDomain.fromPersistence(
                50L, "Arroz", "desc", 50, 11L, "img",
                new BigDecimal("100"), 4L, 1L, LocalDateTime.now(), LocalDateTime.now()
        );
        when(productOutletRepositoryPort.findById(50L)).thenReturn(Optional.of(product));

        ArgumentCaptor<SaleDomain> saleCaptor = ArgumentCaptor.forClass(SaleDomain.class);
        SaleDomain mockSavedSale = SaleDomain.fromPersistence(
                200L, UUID.randomUUID(), new BigDecimal("300"), new BigDecimal("300"), BigDecimal.ZERO,
                8L, 10L, 30L, 4L, 20L, LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), List.of()
        );
        when(saleRepositoryPort.save(saleCaptor.capture())).thenReturn(mockSavedSale);

        MasterRoot payNode = new MasterRoot(8L, "TAR", "Tarjeta", 300L, 1L);
        MasterRoot statusNodeACT = new MasterRoot(10L, "ACT", "Activo", 400L, 1L);
        when(masterTree.getById(8L)).thenReturn(payNode);
        when(masterTree.getById(10L)).thenReturn(statusNodeACT);

        SaleResponse response = useCase.execute(orderCode, "123456", null);

        assertNotNull(response);
        SaleDomain createdSale = saleCaptor.getValue();
        assertEquals(new BigDecimal("300"), createdSale.getAmountReceived());
        assertEquals(BigDecimal.ZERO, createdSale.getChangeGiven());

        verify(orderRepositoryPort, times(1)).save(order);
        assertEquals(101L, order.getStatusId());
    }

    @Test
    @DisplayName("Facturacion exitosa de pedido omnicanal con multiples items e inventarios")
    void testInvoiceOrderSuccessfully_OmnichannelMultipleItems() {
        UUID orderCode = UUID.randomUUID();

        OrderItemDomain item1 = new OrderItemDomain(1L, 50L, 2, "2 UND", new BigDecimal("100"), new BigDecimal("200"), 11L);
        OrderItemDomain item2 = new OrderItemDomain(2L, 51L, 5, "5 UND", new BigDecimal("50"), new BigDecimal("250"), 11L);
        OrderDomain order = OrderDomain.fromPersistence(
                5L, orderCode, new BigDecimal("450"), 8L, 100L, 4L,
                LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), List.of(item1, item2)
        );
        when(orderRepositoryPort.findByCode(orderCode)).thenReturn(Optional.of(order));

        when(currentUserProvider.getCurrentUserContext()).thenReturn(userContext);
        when(currentUserProvider.getCurrentOutletId()).thenReturn(4L);
        when(currentUserProvider.hasRole(anyString())).thenReturn(false);

        when(masterTreeProvider.getTree()).thenReturn(masterTree);
        MasterRoot statusNodePEN = new MasterRoot(100L, "PEN", "Pendiente", 1000L, 1L);
        when(masterTree.getById(100L)).thenReturn(statusNodePEN);

        UserAvalonDomain user = UserAvalonDomain.fromPersistenceBasic(1L, 20L, "employee_user", 1L);
        when(userAvalonRepositoryPort.findByUserName("employee_user")).thenReturn(Optional.of(user));

        PersonDomain client = PersonDomain.createFromEntity(
                30L, "123456", "Juan", "Perez", "Calle 1",
                1L, 1L, 123456L, "juan@email.com", 1L, LocalDateTime.now(), LocalDateTime.now()
        );
        when(personRepositoryPort.findByNumberid("123456")).thenReturn(Optional.of(client));

        when(masterDataRepositoryPort.getIdByCode("ACT")).thenReturn(10L);
        when(masterDataRepositoryPort.getIdByCode("COM")).thenReturn(101L);

        ProductDomain product1 = ProductDomain.fromPersistence(
                50L, "Producto A", "desc A", 100, 11L, "imgA",
                new BigDecimal("100"), 4L, 1L, LocalDateTime.now(), LocalDateTime.now()
        );
        ProductDomain product2 = ProductDomain.fromPersistence(
                51L, "Producto B", "desc B", 50, 11L, "imgB",
                new BigDecimal("50"), 4L, 1L, LocalDateTime.now(), LocalDateTime.now()
        );
        when(productOutletRepositoryPort.findById(50L)).thenReturn(Optional.of(product1));
        when(productOutletRepositoryPort.findById(51L)).thenReturn(Optional.of(product2));

        SaleDomain mockSavedSale = SaleDomain.fromPersistence(
                200L, UUID.randomUUID(), new BigDecimal("450"), new BigDecimal("450"), BigDecimal.ZERO,
                8L, 10L, 30L, 4L, 20L, LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), List.of()
        );
        when(saleRepositoryPort.save(any(SaleDomain.class))).thenReturn(mockSavedSale);

        MasterRoot payNode = new MasterRoot(8L, "EFE", "Efectivo", 300L, 1L);
        MasterRoot statusNodeACT = new MasterRoot(10L, "ACT", "Activo", 400L, 1L);
        when(masterTree.getById(8L)).thenReturn(payNode);
        when(masterTree.getById(10L)).thenReturn(statusNodeACT);

        SaleResponse response = useCase.execute(orderCode, "123456", new BigDecimal("450"));

        assertNotNull(response);
        assertEquals(98, product1.getStock());
        assertEquals(45, product2.getStock());
        verify(productOutletRepositoryPort, times(1)).save(product1);
        verify(productOutletRepositoryPort, times(1)).save(product2);
    }

    @Test
    @DisplayName("Facturacion exitosa por Administrador de Sistema (ROLE_ADMIN) omitiendo validacion de tienda")
    void testInvoiceOrderSuccessfully_AsSystemAdmin_RoleAdmin() {
        UUID orderCode = UUID.randomUUID();

        OrderItemDomain orderItem = new OrderItemDomain(1L, 50L, 1, "1 UND", new BigDecimal("100"), new BigDecimal("100"), 11L);
        OrderDomain order = OrderDomain.fromPersistence(
                5L, orderCode, new BigDecimal("100"), 8L, 100L, 99L,
                LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), List.of(orderItem)
        );
        when(orderRepositoryPort.findByCode(orderCode)).thenReturn(Optional.of(order));

        when(currentUserProvider.getCurrentUserContext()).thenReturn(userContext);
        when(currentUserProvider.hasRole("ROLE_ADMIN")).thenReturn(true);

        when(masterTreeProvider.getTree()).thenReturn(masterTree);
        MasterRoot statusNodePEN = new MasterRoot(100L, "PEN", "Pendiente", 1000L, 1L);
        when(masterTree.getById(100L)).thenReturn(statusNodePEN);

        UserAvalonDomain user = UserAvalonDomain.fromPersistenceBasic(1L, 20L, "employee_user", 1L);
        when(userAvalonRepositoryPort.findByUserName("employee_user")).thenReturn(Optional.of(user));

        PersonDomain client = PersonDomain.createFromEntity(
                30L, "123456", "Juan", "Perez", "Calle 1",
                1L, 1L, 123456L, "juan@email.com", 1L, LocalDateTime.now(), LocalDateTime.now()
        );
        when(personRepositoryPort.findByNumberid("123456")).thenReturn(Optional.of(client));

        when(masterDataRepositoryPort.getIdByCode("ACT")).thenReturn(10L);
        when(masterDataRepositoryPort.getIdByCode("COM")).thenReturn(101L);

        ProductDomain product = ProductDomain.fromPersistence(
                50L, "Arroz", "desc", 100, 11L, "img",
                new BigDecimal("100"), 99L, 1L, LocalDateTime.now(), LocalDateTime.now()
        );
        when(productOutletRepositoryPort.findById(50L)).thenReturn(Optional.of(product));

        SaleDomain mockSavedSale = SaleDomain.fromPersistence(
                200L, UUID.randomUUID(), new BigDecimal("100"), new BigDecimal("100"), BigDecimal.ZERO,
                8L, 10L, 30L, 99L, 20L, LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), List.of()
        );
        when(saleRepositoryPort.save(any(SaleDomain.class))).thenReturn(mockSavedSale);

        MasterRoot payNode = new MasterRoot(8L, "EFE", "Efectivo", 300L, 1L);
        MasterRoot statusNodeACT = new MasterRoot(10L, "ACT", "Activo", 400L, 1L);
        when(masterTree.getById(8L)).thenReturn(payNode);
        when(masterTree.getById(10L)).thenReturn(statusNodeACT);

        SaleResponse response = useCase.execute(orderCode, "123456", new BigDecimal("100"));

        assertNotNull(response);
        verify(currentUserProvider, never()).getCurrentOutletId();
    }

    @Test
    @DisplayName("Facturacion exitosa por Administrador TI (ROLE_ADMINTI) omitiendo validacion de tienda")
    void testInvoiceOrderSuccessfully_AsSystemAdmin_RoleAdminTI() {
        UUID orderCode = UUID.randomUUID();

        OrderItemDomain orderItem = new OrderItemDomain(1L, 50L, 1, "1 UND", new BigDecimal("100"), new BigDecimal("100"), 11L);
        OrderDomain order = OrderDomain.fromPersistence(
                5L, orderCode, new BigDecimal("100"), 8L, 100L, 99L,
                LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), List.of(orderItem)
        );
        when(orderRepositoryPort.findByCode(orderCode)).thenReturn(Optional.of(order));

        when(currentUserProvider.getCurrentUserContext()).thenReturn(userContext);
        when(currentUserProvider.hasRole("ROLE_ADMIN")).thenReturn(false);
        when(currentUserProvider.hasRole("ROLE_ADMINTI")).thenReturn(true);

        when(masterTreeProvider.getTree()).thenReturn(masterTree);
        MasterRoot statusNodePEN = new MasterRoot(100L, "PEN", "Pendiente", 1000L, 1L);
        when(masterTree.getById(100L)).thenReturn(statusNodePEN);

        UserAvalonDomain user = UserAvalonDomain.fromPersistenceBasic(1L, 20L, "employee_user", 1L);
        when(userAvalonRepositoryPort.findByUserName("employee_user")).thenReturn(Optional.of(user));

        PersonDomain client = PersonDomain.createFromEntity(
                30L, "123456", "Juan", "Perez", "Calle 1",
                1L, 1L, 123456L, "juan@email.com", 1L, LocalDateTime.now(), LocalDateTime.now()
        );
        when(personRepositoryPort.findByNumberid("123456")).thenReturn(Optional.of(client));

        when(masterDataRepositoryPort.getIdByCode("ACT")).thenReturn(10L);
        when(masterDataRepositoryPort.getIdByCode("COM")).thenReturn(101L);

        ProductDomain product = ProductDomain.fromPersistence(
                50L, "Arroz", "desc", 100, 11L, "img",
                new BigDecimal("100"), 99L, 1L, LocalDateTime.now(), LocalDateTime.now()
        );
        when(productOutletRepositoryPort.findById(50L)).thenReturn(Optional.of(product));

        SaleDomain mockSavedSale = SaleDomain.fromPersistence(
                200L, UUID.randomUUID(), new BigDecimal("100"), new BigDecimal("100"), BigDecimal.ZERO,
                8L, 10L, 30L, 99L, 20L, LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), List.of()
        );
        when(saleRepositoryPort.save(any(SaleDomain.class))).thenReturn(mockSavedSale);

        MasterRoot payNode = new MasterRoot(8L, "EFE", "Efectivo", 300L, 1L);
        MasterRoot statusNodeACT = new MasterRoot(10L, "ACT", "Activo", 400L, 1L);
        when(masterTree.getById(8L)).thenReturn(payNode);
        when(masterTree.getById(10L)).thenReturn(statusNodeACT);

        SaleResponse response = useCase.execute(orderCode, "123456", new BigDecimal("100"));

        assertNotNull(response);
    }

    @Test
    @DisplayName("Lanza ResourceNotFoundException cuando el pedido no existe")
    void testInvoiceOrder_NotFound_ThrowsResourceNotFoundException() {
        UUID orderCode = UUID.randomUUID();
        when(orderRepositoryPort.findByCode(orderCode)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> useCase.execute(orderCode, "123456", new BigDecimal("200"))
        );

        assertTrue(ex.getMessage().contains("Pedido con código '" + orderCode + "' no encontrado."));
    }

    @Test
    @DisplayName("Lanza BusinessException cuando usuario no admin no tiene tienda configurada en su contexto")
    void testInvoiceOrder_NonAdmin_NullTenantOutlet_ThrowsBusinessException() {
        UUID orderCode = UUID.randomUUID();
        OrderDomain order = OrderDomain.fromPersistence(
                5L, orderCode, new BigDecimal("200"), 8L, 100L, 4L,
                LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), List.of()
        );
        when(orderRepositoryPort.findByCode(orderCode)).thenReturn(Optional.of(order));

        when(currentUserProvider.getCurrentUserContext()).thenReturn(userContext);
        when(currentUserProvider.hasRole(anyString())).thenReturn(false);
        when(currentUserProvider.getCurrentOutletId()).thenReturn(null);

        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> useCase.execute(orderCode, "123456", new BigDecimal("200"))
        );

        assertEquals("No se detectó una tienda asociada en el contexto del empleado actual.", ex.getMessage());
    }

    @Test
    @DisplayName("Lanza BusinessException cuando usuario no admin intenta facturar pedido de otra tienda")
    void testInvoiceOrder_NonAdmin_TenantOutletMismatch_ThrowsBusinessException() {
        UUID orderCode = UUID.randomUUID();
        OrderDomain order = OrderDomain.fromPersistence(
                5L, orderCode, new BigDecimal("200"), 8L, 100L, 5L, // Order is for outlet 5L
                LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), List.of()
        );
        when(orderRepositoryPort.findByCode(orderCode)).thenReturn(Optional.of(order));

        when(currentUserProvider.getCurrentUserContext()).thenReturn(userContext);
        when(currentUserProvider.hasRole(anyString())).thenReturn(false);
        when(currentUserProvider.getCurrentOutletId()).thenReturn(4L); // Employee is in outlet 4L

        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> useCase.execute(orderCode, "123456", new BigDecimal("200"))
        );

        assertEquals("Acceso denegado: No tienes permisos para facturar un pedido de otra tienda.", ex.getMessage());
    }

    @Test
    @DisplayName("Lanza BusinessException cuando el nodo de estado en MasterTree es nulo")
    void testInvoiceOrder_NullStatusNode_ThrowsBusinessException() {
        UUID orderCode = UUID.randomUUID();
        OrderDomain order = OrderDomain.fromPersistence(
                5L, orderCode, new BigDecimal("200"), 8L, 100L, 4L,
                LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), List.of()
        );
        when(orderRepositoryPort.findByCode(orderCode)).thenReturn(Optional.of(order));

        when(currentUserProvider.getCurrentUserContext()).thenReturn(userContext);
        when(currentUserProvider.hasRole(anyString())).thenReturn(false);
        when(currentUserProvider.getCurrentOutletId()).thenReturn(4L);

        when(masterTreeProvider.getTree()).thenReturn(masterTree);
        when(masterTree.getById(100L)).thenReturn(null);

        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> useCase.execute(orderCode, "123456", new BigDecimal("200"))
        );

        assertEquals("El pedido no está en estado PENDIENTE y no puede ser facturado.", ex.getMessage());
    }

    @Test
    @DisplayName("Lanza BusinessException cuando el estado del pedido no es PENDIENTE ('PEN')")
    void testInvoiceOrder_OrderStatusNotPen_ThrowsBusinessException() {
        UUID orderCode = UUID.randomUUID();
        OrderDomain order = OrderDomain.fromPersistence(
                5L, orderCode, new BigDecimal("200"), 8L, 101L, 4L,
                LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), List.of()
        );
        when(orderRepositoryPort.findByCode(orderCode)).thenReturn(Optional.of(order));

        when(currentUserProvider.getCurrentUserContext()).thenReturn(userContext);
        when(currentUserProvider.hasRole(anyString())).thenReturn(false);
        when(currentUserProvider.getCurrentOutletId()).thenReturn(4L);

        when(masterTreeProvider.getTree()).thenReturn(masterTree);
        MasterRoot statusNodeCOM = new MasterRoot(101L, "COM", "Completado", 1000L, 1L);
        when(masterTree.getById(101L)).thenReturn(statusNodeCOM);

        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> useCase.execute(orderCode, "123456", new BigDecimal("200"))
        );

        assertEquals("El pedido no está en estado PENDIENTE y no puede ser facturado.", ex.getMessage());
    }

    @Test
    @DisplayName("Lanza ResourceNotFoundException cuando el usuario autenticado no existe")
    void testInvoiceOrder_UserNotFound_ThrowsResourceNotFoundException() {
        UUID orderCode = UUID.randomUUID();
        OrderDomain order = OrderDomain.fromPersistence(
                5L, orderCode, new BigDecimal("200"), 8L, 100L, 4L,
                LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), List.of()
        );
        when(orderRepositoryPort.findByCode(orderCode)).thenReturn(Optional.of(order));

        when(currentUserProvider.getCurrentUserContext()).thenReturn(userContext);
        when(currentUserProvider.hasRole(anyString())).thenReturn(false);
        when(currentUserProvider.getCurrentOutletId()).thenReturn(4L);

        when(masterTreeProvider.getTree()).thenReturn(masterTree);
        MasterRoot statusNodePEN = new MasterRoot(100L, "PEN", "Pendiente", 1000L, 1L);
        when(masterTree.getById(100L)).thenReturn(statusNodePEN);

        when(userAvalonRepositoryPort.findByUserName("employee_user")).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> useCase.execute(orderCode, "123456", new BigDecimal("200"))
        );

        assertEquals("El usuario autenticado no existe en el sistema", ex.getMessage());
    }

    @Test
    @DisplayName("Lanza BusinessException cuando el usuario autenticado no tiene un registro de persona asociado")
    void testInvoiceOrder_EmployeePersonIdNull_ThrowsBusinessException() {
        UUID orderCode = UUID.randomUUID();
        OrderDomain order = OrderDomain.fromPersistence(
                5L, orderCode, new BigDecimal("200"), 8L, 100L, 4L,
                LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), List.of()
        );
        when(orderRepositoryPort.findByCode(orderCode)).thenReturn(Optional.of(order));

        when(currentUserProvider.getCurrentUserContext()).thenReturn(userContext);
        when(currentUserProvider.hasRole(anyString())).thenReturn(false);
        when(currentUserProvider.getCurrentOutletId()).thenReturn(4L);

        when(masterTreeProvider.getTree()).thenReturn(masterTree);
        MasterRoot statusNodePEN = new MasterRoot(100L, "PEN", "Pendiente", 1000L, 1L);
        when(masterTree.getById(100L)).thenReturn(statusNodePEN);

        UserAvalonDomain userWithoutPerson = UserAvalonDomain.fromPersistenceBasic(1L, null, "employee_user", 1L);
        when(userAvalonRepositoryPort.findByUserName("employee_user")).thenReturn(Optional.of(userWithoutPerson));

        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> useCase.execute(orderCode, "123456", new BigDecimal("200"))
        );

        assertEquals("El usuario actual no tiene un registro de persona (empleado) asociado", ex.getMessage());
    }

    @Test
    @DisplayName("Lanza ResourceNotFoundException cuando el cliente no es encontrado")
    void testInvoiceOrder_ClientNotFound_ThrowsResourceNotFoundException() {
        UUID orderCode = UUID.randomUUID();
        OrderDomain order = OrderDomain.fromPersistence(
                5L, orderCode, new BigDecimal("200"), 8L, 100L, 4L,
                LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), List.of()
        );
        when(orderRepositoryPort.findByCode(orderCode)).thenReturn(Optional.of(order));

        when(currentUserProvider.getCurrentUserContext()).thenReturn(userContext);
        when(currentUserProvider.hasRole(anyString())).thenReturn(false);
        when(currentUserProvider.getCurrentOutletId()).thenReturn(4L);

        when(masterTreeProvider.getTree()).thenReturn(masterTree);
        MasterRoot statusNodePEN = new MasterRoot(100L, "PEN", "Pendiente", 1000L, 1L);
        when(masterTree.getById(100L)).thenReturn(statusNodePEN);

        UserAvalonDomain user = UserAvalonDomain.fromPersistenceBasic(1L, 20L, "employee_user", 1L);
        when(userAvalonRepositoryPort.findByUserName("employee_user")).thenReturn(Optional.of(user));

        when(personRepositoryPort.findByNumberid("999999")).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> useCase.execute(orderCode, "999999", new BigDecimal("200"))
        );

        assertEquals("Cliente con identificación '999999' no encontrado.", ex.getMessage());
    }

    @Test
    @DisplayName("Lanza IllegalStateException cuando el ID del estado ACT no se encuentra en MasterData")
    void testInvoiceOrder_ActiveSaleStatusIdNull_ThrowsIllegalStateException() {
        UUID orderCode = UUID.randomUUID();
        OrderDomain order = OrderDomain.fromPersistence(
                5L, orderCode, new BigDecimal("200"), 8L, 100L, 4L,
                LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), List.of()
        );
        when(orderRepositoryPort.findByCode(orderCode)).thenReturn(Optional.of(order));

        when(currentUserProvider.getCurrentUserContext()).thenReturn(userContext);
        when(currentUserProvider.hasRole(anyString())).thenReturn(false);
        when(currentUserProvider.getCurrentOutletId()).thenReturn(4L);

        when(masterTreeProvider.getTree()).thenReturn(masterTree);
        MasterRoot statusNodePEN = new MasterRoot(100L, "PEN", "Pendiente", 1000L, 1L);
        when(masterTree.getById(100L)).thenReturn(statusNodePEN);

        UserAvalonDomain user = UserAvalonDomain.fromPersistenceBasic(1L, 20L, "employee_user", 1L);
        when(userAvalonRepositoryPort.findByUserName("employee_user")).thenReturn(Optional.of(user));

        PersonDomain client = PersonDomain.createFromEntity(
                30L, "123456", "Juan", "Perez", "Calle 1",
                1L, 1L, 123456L, "juan@email.com", 1L, LocalDateTime.now(), LocalDateTime.now()
        );
        when(personRepositoryPort.findByNumberid("123456")).thenReturn(Optional.of(client));

        when(masterDataRepositoryPort.getIdByCode("ACT")).thenReturn(null);
        when(masterDataRepositoryPort.getIdByCode("COM")).thenReturn(101L);

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> useCase.execute(orderCode, "123456", new BigDecimal("200"))
        );

        assertEquals("Estados ('ACT' o 'COM') no encontrados en MasterData.", ex.getMessage());
    }

    @Test
    @DisplayName("Lanza IllegalStateException cuando el ID del estado COM no se encuentra en MasterData")
    void testInvoiceOrder_CompletedOrderStatusIdNull_ThrowsIllegalStateException() {
        UUID orderCode = UUID.randomUUID();
        OrderDomain order = OrderDomain.fromPersistence(
                5L, orderCode, new BigDecimal("200"), 8L, 100L, 4L,
                LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), List.of()
        );
        when(orderRepositoryPort.findByCode(orderCode)).thenReturn(Optional.of(order));

        when(currentUserProvider.getCurrentUserContext()).thenReturn(userContext);
        when(currentUserProvider.hasRole(anyString())).thenReturn(false);
        when(currentUserProvider.getCurrentOutletId()).thenReturn(4L);

        when(masterTreeProvider.getTree()).thenReturn(masterTree);
        MasterRoot statusNodePEN = new MasterRoot(100L, "PEN", "Pendiente", 1000L, 1L);
        when(masterTree.getById(100L)).thenReturn(statusNodePEN);

        UserAvalonDomain user = UserAvalonDomain.fromPersistenceBasic(1L, 20L, "employee_user", 1L);
        when(userAvalonRepositoryPort.findByUserName("employee_user")).thenReturn(Optional.of(user));

        PersonDomain client = PersonDomain.createFromEntity(
                30L, "123456", "Juan", "Perez", "Calle 1",
                1L, 1L, 123456L, "juan@email.com", 1L, LocalDateTime.now(), LocalDateTime.now()
        );
        when(personRepositoryPort.findByNumberid("123456")).thenReturn(Optional.of(client));

        when(masterDataRepositoryPort.getIdByCode("ACT")).thenReturn(10L);
        when(masterDataRepositoryPort.getIdByCode("COM")).thenReturn(null);

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> useCase.execute(orderCode, "123456", new BigDecimal("200"))
        );

        assertEquals("Estados ('ACT' o 'COM') no encontrados en MasterData.", ex.getMessage());
    }

    @Test
    @DisplayName("Lanza ResourceNotFoundException cuando el producto del item del pedido no existe")
    void testInvoiceOrder_ProductNotFound_ThrowsResourceNotFoundException() {
        UUID orderCode = UUID.randomUUID();

        OrderItemDomain orderItem = new OrderItemDomain(1L, 999L, 2, "2 UND", new BigDecimal("100"), new BigDecimal("200"), 11L);
        OrderDomain order = OrderDomain.fromPersistence(
                5L, orderCode, new BigDecimal("200"), 8L, 100L, 4L,
                LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), List.of(orderItem)
        );
        when(orderRepositoryPort.findByCode(orderCode)).thenReturn(Optional.of(order));

        when(currentUserProvider.getCurrentUserContext()).thenReturn(userContext);
        when(currentUserProvider.hasRole(anyString())).thenReturn(false);
        when(currentUserProvider.getCurrentOutletId()).thenReturn(4L);

        when(masterTreeProvider.getTree()).thenReturn(masterTree);
        MasterRoot statusNodePEN = new MasterRoot(100L, "PEN", "Pendiente", 1000L, 1L);
        when(masterTree.getById(100L)).thenReturn(statusNodePEN);

        UserAvalonDomain user = UserAvalonDomain.fromPersistenceBasic(1L, 20L, "employee_user", 1L);
        when(userAvalonRepositoryPort.findByUserName("employee_user")).thenReturn(Optional.of(user));

        PersonDomain client = PersonDomain.createFromEntity(
                30L, "123456", "Juan", "Perez", "Calle 1",
                1L, 1L, 123456L, "juan@email.com", 1L, LocalDateTime.now(), LocalDateTime.now()
        );
        when(personRepositoryPort.findByNumberid("123456")).thenReturn(Optional.of(client));

        when(masterDataRepositoryPort.getIdByCode("ACT")).thenReturn(10L);
        when(masterDataRepositoryPort.getIdByCode("COM")).thenReturn(101L);

        when(productOutletRepositoryPort.findById(999L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> useCase.execute(orderCode, "123456", new BigDecimal("200"))
        );

        assertEquals("Producto con ID 999 no encontrado", ex.getMessage());
    }

    @Test
    @DisplayName("Lanza DomainValidationException cuando el stock del producto es insuficiente")
    void testInvoiceOrder_InsufficientStock_ThrowsDomainValidationException() {
        UUID orderCode = UUID.randomUUID();

        OrderItemDomain orderItem = new OrderItemDomain(1L, 50L, 10, "10 UND", new BigDecimal("100"), new BigDecimal("1000"), 11L);
        OrderDomain order = OrderDomain.fromPersistence(
                5L, orderCode, new BigDecimal("1000"), 8L, 100L, 4L,
                LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), List.of(orderItem)
        );
        when(orderRepositoryPort.findByCode(orderCode)).thenReturn(Optional.of(order));

        when(currentUserProvider.getCurrentUserContext()).thenReturn(userContext);
        when(currentUserProvider.hasRole(anyString())).thenReturn(false);
        when(currentUserProvider.getCurrentOutletId()).thenReturn(4L);

        when(masterTreeProvider.getTree()).thenReturn(masterTree);
        MasterRoot statusNodePEN = new MasterRoot(100L, "PEN", "Pendiente", 1000L, 1L);
        when(masterTree.getById(100L)).thenReturn(statusNodePEN);

        UserAvalonDomain user = UserAvalonDomain.fromPersistenceBasic(1L, 20L, "employee_user", 1L);
        when(userAvalonRepositoryPort.findByUserName("employee_user")).thenReturn(Optional.of(user));

        PersonDomain client = PersonDomain.createFromEntity(
                30L, "123456", "Juan", "Perez", "Calle 1",
                1L, 1L, 123456L, "juan@email.com", 1L, LocalDateTime.now(), LocalDateTime.now()
        );
        when(personRepositoryPort.findByNumberid("123456")).thenReturn(Optional.of(client));

        when(masterDataRepositoryPort.getIdByCode("ACT")).thenReturn(10L);
        when(masterDataRepositoryPort.getIdByCode("COM")).thenReturn(101L);

        ProductDomain productWithLowStock = ProductDomain.fromPersistence(
                50L, "Arroz", "desc", 2, 11L, "img",
                new BigDecimal("100"), 4L, 1L, LocalDateTime.now(), LocalDateTime.now()
        );
        when(productOutletRepositoryPort.findById(50L)).thenReturn(Optional.of(productWithLowStock));

        DomainValidationException ex = assertThrows(
                DomainValidationException.class,
                () -> useCase.execute(orderCode, "123456", new BigDecimal("1000"))
        );

        assertTrue(ex.getMessage().contains("Insufficient stock for product"));
    }

    @Test
    @DisplayName("Lanza BusinessException cuando el monto recibido es menor que el total de la venta")
    void testInvoiceOrder_AmountReceivedLessThanTotal_ThrowsBusinessException() {
        UUID orderCode = UUID.randomUUID();

        OrderItemDomain orderItem = new OrderItemDomain(1L, 50L, 2, "2 UND", new BigDecimal("100"), new BigDecimal("200"), 11L);
        OrderDomain order = OrderDomain.fromPersistence(
                5L, orderCode, new BigDecimal("200"), 8L, 100L, 4L,
                LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), List.of(orderItem)
        );
        when(orderRepositoryPort.findByCode(orderCode)).thenReturn(Optional.of(order));

        when(currentUserProvider.getCurrentUserContext()).thenReturn(userContext);
        when(currentUserProvider.hasRole(anyString())).thenReturn(false);
        when(currentUserProvider.getCurrentOutletId()).thenReturn(4L);

        when(masterTreeProvider.getTree()).thenReturn(masterTree);
        MasterRoot statusNodePEN = new MasterRoot(100L, "PEN", "Pendiente", 1000L, 1L);
        when(masterTree.getById(100L)).thenReturn(statusNodePEN);

        UserAvalonDomain user = UserAvalonDomain.fromPersistenceBasic(1L, 20L, "employee_user", 1L);
        when(userAvalonRepositoryPort.findByUserName("employee_user")).thenReturn(Optional.of(user));

        PersonDomain client = PersonDomain.createFromEntity(
                30L, "123456", "Juan", "Perez", "Calle 1",
                1L, 1L, 123456L, "juan@email.com", 1L, LocalDateTime.now(), LocalDateTime.now()
        );
        when(personRepositoryPort.findByNumberid("123456")).thenReturn(Optional.of(client));

        when(masterDataRepositoryPort.getIdByCode("ACT")).thenReturn(10L);
        when(masterDataRepositoryPort.getIdByCode("COM")).thenReturn(101L);

        ProductDomain product = ProductDomain.fromPersistence(
                50L, "Arroz", "desc", 100, 11L, "img",
                new BigDecimal("100"), 4L, 1L, LocalDateTime.now(), LocalDateTime.now()
        );
        when(productOutletRepositoryPort.findById(50L)).thenReturn(Optional.of(product));

        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> useCase.execute(orderCode, "123456", new BigDecimal("150"))
        );

        assertTrue(ex.getMessage().contains("es menor que el valor total a pagar"));
    }
}
