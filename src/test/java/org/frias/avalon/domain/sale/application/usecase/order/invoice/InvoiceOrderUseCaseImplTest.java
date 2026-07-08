package org.frias.avalon.domain.sale.application.usecase.order.invoice;

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
import org.frias.avalon.domain.sale.application.dto.response.SaleResponse;
import org.frias.avalon.domain.sale.application.port.OrderRepositoryPort;
import org.frias.avalon.domain.sale.application.port.SaleRepositoryPort;
import org.frias.avalon.domain.sale.domain.OrderDomain;
import org.frias.avalon.domain.sale.domain.OrderItemDomain;
import org.frias.avalon.domain.sale.domain.SaleDomain;
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
    void testInvoiceOrderSuccessfully() {
        UUID orderCode = UUID.randomUUID();

        // Mocking Order loading (Status PEN = 100L)
        OrderItemDomain orderItem = new OrderItemDomain(1L, 50L, 2, "2 UND", new BigDecimal("100"), new BigDecimal("200"), 11L);
        OrderDomain order = OrderDomain.fromPersistence(
                5L, orderCode, new BigDecimal("200"), 8L, 100L, 4L,
                LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), List.of(orderItem)
        );
        when(orderRepositoryPort.findByCode(orderCode)).thenReturn(Optional.of(order));

        // Mocking Security context
        when(currentUserProvider.getCurrentUserContext()).thenReturn(userContext);
        when(currentUserProvider.getCurrentOutletId()).thenReturn(4L);
        when(currentUserProvider.hasRole(anyString())).thenReturn(false);

        // Mocking MasterTree status resolving
        when(masterTreeProvider.getTree()).thenReturn(masterTree);
        MasterRoot statusNodePEN = new MasterRoot(100L, "PEN", "Pendiente", 1000L, 1L);
        when(masterTree.getById(100L)).thenReturn(statusNodePEN);

        // Mocking Employee resolving
        UserAvalonDomain user = UserAvalonDomain.fromPersistenceBasic(1L, 20L, "employee_user", 1L);
        when(userAvalonRepositoryPort.findByUserName("employee_user")).thenReturn(Optional.of(user));

        // Mocking Client resolving
        PersonDomain client = PersonDomain.createFromEntity(
                30L, "123456", "Juan", "Perez", "Calle 1",
                1L, 1L, 123456L, "juan@email.com", 1L, LocalDateTime.now(), LocalDateTime.now()
        );
        when(personRepositoryPort.findByNumberid("123456")).thenReturn(Optional.of(client));

        // Mocking master data ids for statuses
        when(masterDataRepositoryPort.getIdByCode("ACT")).thenReturn(10L); // Venta ACT
        when(masterDataRepositoryPort.getIdByCode("COM")).thenReturn(101L); // Pedido Completado

        // Mocking Product resolving and stock discount
        ProductDomain product = ProductDomain.fromPersistence(
                50L, "Arroz", "desc", 100, 11L, "img",
                new BigDecimal("100"), 4L, 1L, LocalDateTime.now(), LocalDateTime.now()
        );
        when(productOutletRepositoryPort.findById(50L)).thenReturn(Optional.of(product));

        // Mocking Sale saving
        SaleDomain mockSavedSale = SaleDomain.fromPersistence(
                200L, UUID.randomUUID(), new BigDecimal("200"), new BigDecimal("200"), BigDecimal.ZERO,
                8L, 10L, 30L, 4L, 20L, LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), List.of()
        );
        when(saleRepositoryPort.save(any(SaleDomain.class))).thenReturn(mockSavedSale);

        // Mocking responses mapping
        MasterRoot payNode = new MasterRoot(8L, "EFE", "Efectivo", 300L, 1L);
        MasterRoot statusNodeACT = new MasterRoot(10L, "ACT", "Activo", 400L, 1L);
        when(masterTree.getById(8L)).thenReturn(payNode);
        when(masterTree.getById(10L)).thenReturn(statusNodeACT);

        SaleResponse response = useCase.execute(orderCode, "123456", new BigDecimal("200"));

        assertNotNull(response);
        assertEquals(200L, response.id());
        assertEquals(new BigDecimal("200"), response.totalAmount());
        assertEquals("Juan Perez", response.clientFullName());

        // Verify product stock saved
        verify(productOutletRepositoryPort, times(1)).save(any(ProductDomain.class));
        // Verify sale saved
        verify(saleRepositoryPort, times(1)).save(any(SaleDomain.class));
        // Verify order saved to change status
        verify(orderRepositoryPort, times(1)).save(any(OrderDomain.class));
        // Verify order status changed to COM
        assertEquals(101L, order.getStatusId());
    }

    @Test
    void testInvoiceAlreadyInvoicedThrowsException() {
        UUID orderCode = UUID.randomUUID();

        // Order is in COM status (101L), not PEN (100L)
        OrderItemDomain orderItem = new OrderItemDomain(1L, 50L, 2, "2 UND", new BigDecimal("100"), new BigDecimal("200"), 11L);
        OrderDomain order = OrderDomain.fromPersistence(
                5L, orderCode, new BigDecimal("200"), 8L, 101L, 4L,
                LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), List.of(orderItem)
        );
        when(orderRepositoryPort.findByCode(orderCode)).thenReturn(Optional.of(order));

        when(currentUserProvider.getCurrentUserContext()).thenReturn(userContext);
        when(currentUserProvider.getCurrentOutletId()).thenReturn(4L);
        when(currentUserProvider.hasRole(anyString())).thenReturn(false);

        when(masterTreeProvider.getTree()).thenReturn(masterTree);
        MasterRoot statusNodeCOM = new MasterRoot(101L, "COM", "Completado", 1000L, 1L);
        when(masterTree.getById(101L)).thenReturn(statusNodeCOM);

        assertThrows(BusinessException.class, () -> useCase.execute(orderCode, "123456", new BigDecimal("200")));
    }
}
