package org.frias.avalon.domain.order.application.usecase;

import org.frias.avalon.core.exeptions.ResourceNotFoundException;
import org.frias.avalon.domain.masterdata.domain.model.MasterRoot;
import org.frias.avalon.domain.masterdata.domain.model.MasterTree;
import org.frias.avalon.domain.masterdata.domain.service.MasterTreeProvider;
import org.frias.avalon.domain.order.application.dto.OrderResponse;
import org.frias.avalon.domain.order.application.port.OrderRepositoryPort;
import org.frias.avalon.domain.order.domain.OrderDomain;
import org.frias.avalon.domain.order.domain.OrderItemDomain;
import org.frias.avalon.domain.order.domain.OrderStatusHistoryDomain;
import org.frias.avalon.domain.order.infrastructure.persistence.mapper.OrderMapper;
import org.frias.avalon.domain.order.presentation.controller.OrderWebSocketController;
import org.frias.avalon.domain.outlet.domain.model.OutletDomain;
import org.frias.avalon.domain.outlet.domain.port.OutletRepositoryPort;
import org.frias.avalon.domain.product.infraestructure.entity.ProductOutlet;
import org.frias.avalon.domain.product.infraestructure.repository.JpaProductOutletRepository;
import org.frias.avalon.domain.sale.application.port.SaleRepositoryPort;
import org.frias.avalon.domain.sale.domain.SaleDomain;
import org.frias.avalon.core.tenant.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
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

@DisplayName("Unit Tests for DeliverOrderByQrUseCaseImpl")
class DeliverOrderByQrUseCaseImplTest {

    private OrderRepositoryPort orderRepositoryPort;
    private MasterTreeProvider masterTreeProvider;
    private JpaProductOutletRepository jpaProductOutletRepository;
    private SaleRepositoryPort saleRepositoryPort;
    private OrderMapper orderMapper;
    private OrderWebSocketController orderWebSocketController;
    private OutletRepositoryPort outletRepositoryPort;
    private PlatformTransactionManager transactionManager;

    private DeliverOrderByQrUseCaseImpl deliverOrderByQrUseCase;
    private MasterTree masterTree;

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @BeforeEach
    void setUp() {
        TenantContext.clear();
        TenantContext.setTenantOutletId(1L);
        orderRepositoryPort = mock(OrderRepositoryPort.class);
        masterTreeProvider = mock(MasterTreeProvider.class);
        jpaProductOutletRepository = mock(JpaProductOutletRepository.class);
        saleRepositoryPort = mock(SaleRepositoryPort.class);
        orderMapper = mock(OrderMapper.class);
        orderWebSocketController = mock(OrderWebSocketController.class);
        outletRepositoryPort = mock(OutletRepositoryPort.class);
        transactionManager = mock(PlatformTransactionManager.class);

        TransactionStatus transactionStatus = mock(TransactionStatus.class);
        when(transactionManager.getTransaction(any())).thenReturn(transactionStatus);

        OutletDomain mockOutlet = mock(OutletDomain.class);
        when(mockOutlet.getId()).thenReturn(1L);
        when(mockOutlet.getCompanyId()).thenReturn(10L);
        when(outletRepositoryPort.findById(any())).thenReturn(Optional.of(mockOutlet));

        MasterRoot entNode = new MasterRoot(104L, "ENT", "ENTREGADO", null, 1L);
        MasterRoot ordDispNode = new MasterRoot(16L, "ORD_DISP", "DESPACHADO", null, 1L);
        MasterRoot ordCanNode = new MasterRoot(17L, "ORD_CAN", "CANCELADO", null, 1L);
        MasterRoot payPadNode = new MasterRoot(102L, "PAY_PAD", "PAGADO", null, 1L);
        MasterRoot actNode = new MasterRoot(1L, "ACT", "ACTIVO", null, 1L);
        MasterRoot cashNode = new MasterRoot(201L, "CASH", "EFECTIVO", null, 1L);
        MasterRoot undNode = new MasterRoot(20L, "UND", "UNIDAD", null, 1L);
        MasterRoot efeNode = new MasterRoot(140L, "EFE", "EFECTIVO", null, 1L);

        masterTree = new MasterTree(List.of(entNode, ordDispNode, ordCanNode, payPadNode, actNode, cashNode, undNode, efeNode));
        when(masterTreeProvider.getTree()).thenReturn(masterTree);

        deliverOrderByQrUseCase = new DeliverOrderByQrUseCaseImpl(
                orderRepositoryPort,
                masterTreeProvider,
                jpaProductOutletRepository,
                saleRepositoryPort,
                orderMapper,
                orderWebSocketController,
                outletRepositoryPort,
                transactionManager
        );
    }

    @Test
    @DisplayName("Should successfully deliver order, deduct stock and broadcast via WebSocket")
    void execute_Success_WhenValidOrderCode() {
        String orderCode = "ORD-A1B2C3D4-E5F6-7890-ABCD-1234567890EF";
        Long userId = 99L;
        Long previousStatusId = 16L; // Ready / Dispatched

        OrderItemDomain item = OrderItemDomain.builder()
                .id(1L)
                .productOutletId(50L)
                .quantity(3)
                .unitPrice(new BigDecimal("10.00"))
                .subtotal(new BigDecimal("30.00"))
                .build();

        OrderDomain order = OrderDomain.builder()
                .id(500L)
                .orderCode(orderCode)
                .outletId(1L)
                .customerId(10L)
                .orderStatusId(previousStatusId)
                .paymentStatusId(1L)
                .total(new BigDecimal("30.00"))
                .items(List.of(item))
                .build();

        when(orderRepositoryPort.findByOrderCode(orderCode)).thenReturn(Optional.of(order));

        ProductOutlet productEntity = new ProductOutlet();
        productEntity.setId(50L);
        productEntity.setStock(20);
        productEntity.setUnitMeasureId(1L);
        when(jpaProductOutletRepository.findById(50L)).thenReturn(Optional.of(productEntity));

        when(orderRepositoryPort.save(any(OrderDomain.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderResponse mockResponse = OrderResponse.builder()
                .id(500L)
                .orderCode(orderCode)
                .orderStatusId(104L)
                .paymentStatusId(102L)
                .total(new BigDecimal("30.00"))
                .build();
        when(orderMapper.toResponse(any(OrderDomain.class))).thenReturn(mockResponse);

        OrderResponse result = deliverOrderByQrUseCase.execute(orderCode, userId);

        assertNotNull(result);
        assertEquals(104L, result.getOrderStatusId());
        assertEquals(102L, result.getPaymentStatusId());

        // Stock deduction check: 20 - 3 = 17
        assertEquals(17, productEntity.getStock());
        verify(jpaProductOutletRepository, times(1)).save(productEntity);

        // Status history check
        ArgumentCaptor<OrderStatusHistoryDomain> historyCaptor = ArgumentCaptor.forClass(OrderStatusHistoryDomain.class);
        verify(orderRepositoryPort, times(1)).saveStatusHistory(historyCaptor.capture());
        assertEquals(500L, historyCaptor.getValue().getOrderId());
        assertEquals(previousStatusId, historyCaptor.getValue().getPreviousStatusId());
        assertEquals(104L, historyCaptor.getValue().getNewStatusId());
        assertEquals(userId, historyCaptor.getValue().getChangedByUserId());

        // Sale emission check
        ArgumentCaptor<SaleDomain> saleCaptor = ArgumentCaptor.forClass(SaleDomain.class);
        verify(saleRepositoryPort, times(1)).save(saleCaptor.capture());
        SaleDomain emittedSale = saleCaptor.getValue();
        assertNotNull(emittedSale);
        assertEquals(new BigDecimal("30.00"), emittedSale.getTotalAmount());
        assertEquals(new BigDecimal("30.00"), emittedSale.getAmountReceived());
        assertEquals(userId, emittedSale.getEmployeeId());
        assertEquals(1L, emittedSale.getOutletId());
        assertEquals(1, emittedSale.getItems().size());

        // WebSocket checks
        verify(orderWebSocketController, times(1)).broadcastOrderStatusChanged(eq(500L), eq(mockResponse));
        verify(orderWebSocketController, times(1)).broadcastOrderCreated(eq(1L), eq(mockResponse));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when order does not exist")
    void execute_ThrowsException_WhenOrderNotFound() {
        String orderCode = "ORD-NOT-FOUND";
        when(orderRepositoryPort.findByOrderCode(orderCode)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> deliverOrderByQrUseCase.execute(orderCode, 1L));
    }

    @Test
    @DisplayName("Should throw IllegalStateException when order is already delivered")
    void execute_ThrowsException_WhenOrderAlreadyDelivered() {
        String orderCode = "ORD-ALREADY-DELIVERED";
        OrderDomain order = OrderDomain.builder()
                .id(501L)
                .orderCode(orderCode)
                .orderStatusId(104L) // 104L is ORD_DEL
                .build();

        when(orderRepositoryPort.findByOrderCode(orderCode)).thenReturn(Optional.of(order));

        assertThrows(IllegalStateException.class, () -> deliverOrderByQrUseCase.execute(orderCode, 1L));
    }

    @Test
    @DisplayName("Should throw IllegalStateException when order is cancelled or rejected")
    void execute_ThrowsException_WhenOrderCancelled() {
        String orderCode = "ORD-CANCELLED";
        OrderDomain order = OrderDomain.builder()
                .id(502L)
                .orderCode(orderCode)
                .orderStatusId(17L) // 17L is ORD_CAN
                .build();

        when(orderRepositoryPort.findByOrderCode(orderCode)).thenReturn(Optional.of(order));

        assertThrows(IllegalStateException.class, () -> deliverOrderByQrUseCase.execute(orderCode, 1L));
    }
}
