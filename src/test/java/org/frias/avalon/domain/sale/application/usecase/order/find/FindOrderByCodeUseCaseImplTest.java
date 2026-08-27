package org.frias.avalon.domain.sale.application.usecase.order.find;

import org.frias.avalon.core.exeptions.ResourceNotFoundException;
import org.frias.avalon.domain.masterdata.domain.model.MasterRoot;
import org.frias.avalon.domain.masterdata.domain.model.MasterTree;
import org.frias.avalon.domain.masterdata.domain.service.MasterTreeProvider;
import org.frias.avalon.domain.product.application.port.ProductOutletRepositoryPort;
import org.frias.avalon.domain.product.domain.ProductDomain;
import org.frias.avalon.domain.sale.application.dto.response.OrderResponse;
import org.frias.avalon.domain.sale.application.port.OrderRepositoryPort;
import org.frias.avalon.domain.sale.domain.OrderDomain;
import org.frias.avalon.domain.sale.domain.OrderItemDomain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Unit Tests for FindOrderByCodeUseCaseImpl")
class FindOrderByCodeUseCaseImplTest {

    @Mock
    private OrderRepositoryPort orderRepositoryPort;

    @Mock
    private ProductOutletRepositoryPort productOutletRepositoryPort;

    @Mock
    private MasterTreeProvider masterTreeProvider;

    private FindOrderByCodeUseCaseImpl findOrderByCodeUseCase;

    @BeforeEach
    void setUp() {
        findOrderByCodeUseCase = new FindOrderByCodeUseCaseImpl(
                orderRepositoryPort,
                productOutletRepositoryPort,
                masterTreeProvider
        );
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when order does not exist")
    void shouldThrowResourceNotFoundExceptionWhenOrderNotFound() {
        UUID code = UUID.randomUUID();
        when(orderRepositoryPort.findByCode(code)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () ->
                findOrderByCodeUseCase.execute(code)
        );

        assertTrue(ex.getMessage().contains("Pedido con código '" + code + "' no encontrado."));
        verify(orderRepositoryPort, times(1)).findByCode(code);
        verifyNoInteractions(masterTreeProvider, productOutletRepositoryPort);
    }

    @Test
    @DisplayName("Should return OrderResponse successfully when order, products, and master data exist")
    void shouldReturnOrderResponseSuccessfully() {
        UUID code = UUID.randomUUID();
        Long orderId = 100L;
        Long payMethodId = 1L;
        Long statusId = 2L;
        Long outletId = 5L;
        LocalDateTime now = LocalDateTime.now();

        OrderItemDomain itemDomain = new OrderItemDomain(
                10L, 50L, 2, "2 UND", new BigDecimal("15.00"), new BigDecimal("30.00"), 3L
        );

        OrderDomain orderDomain = OrderDomain.fromPersistence(
                orderId, code, new BigDecimal("30.00"), payMethodId, statusId, outletId, now, now, now, List.of(itemDomain)
        );

        MasterRoot payNode = MasterRoot.fromPersistence(payMethodId, "EFE", "Efectivo", null, 99L);
        MasterRoot statusNode = MasterRoot.fromPersistence(statusId, "PEN", "Pendiente", null, 99L);
        MasterTree tree = new MasterTree(List.of(payNode, statusNode));

        ProductDomain productDomain = ProductDomain.fromPersistence(
                50L, "Arroz", "Arroz 1kg", 100, 3L, "http://image", new BigDecimal("15.00"), outletId, 99L, now, now
        );

        when(orderRepositoryPort.findByCode(code)).thenReturn(Optional.of(orderDomain));
        when(masterTreeProvider.getTree()).thenReturn(tree);
        when(productOutletRepositoryPort.findById(50L)).thenReturn(Optional.of(productDomain));

        OrderResponse response = findOrderByCodeUseCase.execute(code);

        assertNotNull(response);
        assertEquals(orderId, response.id());
        assertEquals(code, response.orderCode());
        assertEquals(new BigDecimal("30.00"), response.totalAmount());
        assertEquals(now, response.orderDate());
        assertEquals(outletId, response.outletId());

        assertNotNull(response.paymentMethod());
        assertEquals(payMethodId, response.paymentMethod().id());
        assertEquals("EFE", response.paymentMethod().shortName());
        assertEquals("Efectivo", response.paymentMethod().fullName());

        assertNotNull(response.status());
        assertEquals(statusId, response.status().id());
        assertEquals("PEN", response.status().shortName());
        assertEquals("Pendiente", response.status().fullName());

        assertEquals(1, response.items().size());
        assertEquals(50L, response.items().get(0).productId());
        assertEquals("Arroz", response.items().get(0).productName());
        assertEquals("2 UND", response.items().get(0).displayQuantity());
        assertEquals(new BigDecimal("15.00"), response.items().get(0).unitPrice());
        assertEquals(new BigDecimal("30.00"), response.items().get(0).subtotal());
    }

    @Test
    @DisplayName("Should return OrderResponse with null paymentMethod and status DTOs when nodes are missing in MasterTree")
    void shouldReturnOrderResponseWithNullDtosWhenNodesMissingInTree() {
        UUID code = UUID.randomUUID();
        Long orderId = 101L;
        Long payMethodId = 99L;
        Long statusId = 98L;
        Long outletId = 5L;
        LocalDateTime now = LocalDateTime.now();

        OrderItemDomain itemDomain = new OrderItemDomain(
                11L, 50L, 1, "1 UND", new BigDecimal("10.00"), new BigDecimal("10.00"), 3L
        );

        OrderDomain orderDomain = OrderDomain.fromPersistence(
                orderId, code, new BigDecimal("10.00"), payMethodId, statusId, outletId, now, now, now, List.of(itemDomain)
        );

        MasterTree emptyTree = new MasterTree(Collections.emptyList());

        ProductDomain productDomain = ProductDomain.fromPersistence(
                50L, "Frijol", "Frijol", 50, 3L, null, new BigDecimal("10.00"), outletId, 99L, now, now
        );

        when(orderRepositoryPort.findByCode(code)).thenReturn(Optional.of(orderDomain));
        when(masterTreeProvider.getTree()).thenReturn(emptyTree);
        when(productOutletRepositoryPort.findById(50L)).thenReturn(Optional.of(productDomain));

        OrderResponse response = findOrderByCodeUseCase.execute(code);

        assertNotNull(response);
        assertNull(response.paymentMethod());
        assertNull(response.status());
        assertEquals(1, response.items().size());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when product of an order item is not found")
    void shouldThrowResourceNotFoundExceptionWhenProductNotFound() {
        UUID code = UUID.randomUUID();
        Long orderId = 102L;
        Long payMethodId = 1L;
        Long statusId = 2L;
        Long outletId = 5L;
        LocalDateTime now = LocalDateTime.now();

        OrderItemDomain itemDomain = new OrderItemDomain(
                12L, 999L, 1, "1 UND", new BigDecimal("10.00"), new BigDecimal("10.00"), 3L
        );

        OrderDomain orderDomain = OrderDomain.fromPersistence(
                orderId, code, new BigDecimal("10.00"), payMethodId, statusId, outletId, now, now, now, List.of(itemDomain)
        );

        MasterTree tree = new MasterTree(Collections.emptyList());

        when(orderRepositoryPort.findByCode(code)).thenReturn(Optional.of(orderDomain));
        when(masterTreeProvider.getTree()).thenReturn(tree);
        when(productOutletRepositoryPort.findById(999L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () ->
                findOrderByCodeUseCase.execute(code)
        );

        assertEquals("Producto con ID 999 no encontrado", ex.getMessage());
    }
}
