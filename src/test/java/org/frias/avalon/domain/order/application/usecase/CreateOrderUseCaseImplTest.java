package org.frias.avalon.domain.order.application.usecase;

import org.frias.avalon.core.permissions.CurrentUserProviderPort;
import org.frias.avalon.domain.masterdata.domain.repository.MasterDataRepositoryPort;
import org.frias.avalon.domain.masterdata.domain.service.MasterTreeProvider;
import org.frias.avalon.domain.order.application.dto.CreateOrderRequest;
import org.frias.avalon.domain.order.application.dto.OrderItemRequest;
import org.frias.avalon.domain.order.application.dto.OrderResponse;
import org.frias.avalon.domain.order.application.port.OrderRepositoryPort;
import org.frias.avalon.domain.order.domain.OrderDomain;
import org.frias.avalon.domain.order.infrastructure.persistence.mapper.OrderMapper;
import org.frias.avalon.domain.order.presentation.controller.OrderWebSocketController;
import org.frias.avalon.domain.product.domain.service.UnitConversionService;
import org.frias.avalon.domain.product.infraestructure.entity.ProductOutlet;
import org.frias.avalon.domain.product.infraestructure.repository.JpaProductOutletRepository;
import org.frias.avalon.domain.outlet.domain.model.OutletDomain;
import org.frias.avalon.domain.outlet.domain.port.OutletRepositoryPort;
import org.frias.avalon.domain.user.domain.port.UserAvalonRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@DisplayName("Unit Tests for CreateOrderUseCaseImpl in Omnichannel Order Domain")
class CreateOrderUseCaseImplTest {

    private OrderRepositoryPort orderRepositoryPort;
    private MasterDataRepositoryPort masterDataRepositoryPort;
    private JpaProductOutletRepository jpaProductOutletRepository;
    private OrderMapper orderMapper;
    private OrderWebSocketController orderWebSocketController;
    private CurrentUserProviderPort currentUserProvider;
    private UserAvalonRepositoryPort userAvalonRepositoryPort;
    private MasterTreeProvider masterTreeProvider;
    private UnitConversionService unitConversionService;
    private OutletRepositoryPort outletRepositoryPort;
    private org.springframework.transaction.PlatformTransactionManager transactionManager;
    private org.frias.avalon.domain.order.infrastructure.persistence.repository.JpaOrderRepository jpaOrderRepository;

    private CreateOrderUseCaseImpl createOrderUseCase;

    @BeforeEach
    void setUp() {
        orderRepositoryPort = mock(OrderRepositoryPort.class);
        masterDataRepositoryPort = mock(MasterDataRepositoryPort.class);
        jpaProductOutletRepository = mock(JpaProductOutletRepository.class);
        jpaOrderRepository = mock(org.frias.avalon.domain.order.infrastructure.persistence.repository.JpaOrderRepository.class);
        orderMapper = mock(OrderMapper.class);
        orderWebSocketController = mock(OrderWebSocketController.class);
        currentUserProvider = mock(CurrentUserProviderPort.class);
        userAvalonRepositoryPort = mock(UserAvalonRepositoryPort.class);
        masterTreeProvider = mock(MasterTreeProvider.class);
        unitConversionService = mock(UnitConversionService.class);
        outletRepositoryPort = mock(OutletRepositoryPort.class);
        transactionManager = mock(org.springframework.transaction.PlatformTransactionManager.class);

        org.springframework.transaction.TransactionStatus transactionStatus = mock(org.springframework.transaction.TransactionStatus.class);
        when(transactionManager.getTransaction(any())).thenReturn(transactionStatus);

        OutletDomain mockOutlet = mock(OutletDomain.class);
        when(mockOutlet.getId()).thenReturn(1L);
        when(mockOutlet.getCompanyId()).thenReturn(100L);
        when(outletRepositoryPort.findById(any())).thenReturn(Optional.of(mockOutlet));

        createOrderUseCase = new CreateOrderUseCaseImpl(
                orderRepositoryPort,
                masterDataRepositoryPort,
                jpaProductOutletRepository,
                jpaOrderRepository,
                orderMapper,
                orderWebSocketController,
                currentUserProvider,
                userAvalonRepositoryPort,
                masterTreeProvider,
                unitConversionService,
                outletRepositoryPort,
                transactionManager
        );
    }

    @Test
    @DisplayName("Should create omnichannel order and broadcast via WebSocket")
    void shouldCreateOrderAndBroadcastWebSocket() {
        OrderItemRequest itemReq = new OrderItemRequest();
        itemReq.setProductOutletId(10L);
        itemReq.setQuantity(2);
        itemReq.setUnitPrice(new BigDecimal("25.00"));

        CreateOrderRequest request = new CreateOrderRequest();
        request.setCustomerId(5L);
        request.setOutletId(1L);
        request.setPaymentMethodId(2L);
        request.setItems(List.of(itemReq));

        when(masterDataRepositoryPort.getIdByCode("ORD_PEN")).thenReturn(101L);
        when(masterDataRepositoryPort.getIdByCode("PAY_PEN")).thenReturn(102L);
        when(masterDataRepositoryPort.getIdByCode("PEN")).thenReturn(103L);

        ProductOutlet productOutlet = new ProductOutlet();
        productOutlet.setId(10L);
        productOutlet.setLocalName("Galletas Integral");
        productOutlet.setStock(50);
        when(jpaProductOutletRepository.findById(10L)).thenReturn(Optional.of(productOutlet));
        when(jpaOrderRepository.sumQuantityByProductOutletIdAndStatusIn(eq(10L), anyList())).thenReturn(0);

        OrderDomain savedDomain = OrderDomain.builder()
                .id(1L)
                .orderCode("ORD-A1B2C3D4")
                .customerId(5L)
                .outletId(1L)
                .subtotal(new BigDecimal("50.00"))
                .tax(new BigDecimal("9.50"))
                .total(new BigDecimal("59.50"))
                .build();

        when(orderRepositoryPort.save(any(OrderDomain.class))).thenReturn(savedDomain);

        OrderResponse mockResponse = OrderResponse.builder()
                .id(1L)
                .orderCode("ORD-A1B2C3D4")
                .outletId(1L)
                .total(new BigDecimal("59.50"))
                .build();

        when(orderMapper.toResponse(savedDomain)).thenReturn(mockResponse);

        OrderResponse result = createOrderUseCase.execute(request);

        assertNotNull(result);
        assertEquals("ORD-A1B2C3D4", result.getOrderCode());
        assertEquals(1L, result.getOutletId());
        verify(orderRepositoryPort, times(1)).save(any(OrderDomain.class));
        verify(orderWebSocketController, times(1)).broadcastOrderCreated(eq(1L), eq(mockResponse));
    }

    @Test
    @DisplayName("Should throw InsufficientStockException when requested quantity exceeds available stock")
    void shouldThrowInsufficientStockExceptionWhenStockInsufficient() {
        OrderItemRequest itemReq = new OrderItemRequest();
        itemReq.setProductOutletId(10L);
        itemReq.setQuantity(5);
        itemReq.setUnitPrice(new BigDecimal("25.00"));

        CreateOrderRequest request = new CreateOrderRequest();
        request.setCustomerId(5L);
        request.setOutletId(1L);
        request.setPaymentMethodId(2L);
        request.setItems(List.of(itemReq));

        ProductOutlet productOutlet = new ProductOutlet();
        productOutlet.setId(10L);
        productOutlet.setLocalName("Limon Criollo");
        productOutlet.setStock(4);
        when(jpaProductOutletRepository.findById(10L)).thenReturn(Optional.of(productOutlet));
        when(jpaOrderRepository.sumQuantityByProductOutletIdAndStatusIn(eq(10L), anyList())).thenReturn(0);

        assertThrows(org.frias.avalon.core.exeptions.InsufficientStockException.class, () -> createOrderUseCase.execute(request));
    }

    @Test
    @DisplayName("Should switch tenant to order outlet and restore previous tenant after execution")
    void shouldSwitchAndRestoreTenantContext() {
        org.frias.avalon.core.tenant.TenantContext.setTenantId(99L);
        org.frias.avalon.core.tenant.TenantContext.setTenantOutletId(77L);

        OrderItemRequest itemReq = new OrderItemRequest();
        itemReq.setProductOutletId(10L);
        itemReq.setQuantity(1);
        itemReq.setUnitPrice(new BigDecimal("10.00"));

        CreateOrderRequest request = new CreateOrderRequest();
        request.setOutletId(1L);
        request.setItems(List.of(itemReq));

        ProductOutlet productOutlet = new ProductOutlet();
        productOutlet.setId(10L);
        productOutlet.setStock(10);
        when(jpaProductOutletRepository.findById(10L)).thenReturn(Optional.of(productOutlet));
        when(jpaOrderRepository.sumQuantityByProductOutletIdAndStatusIn(eq(10L), anyList())).thenReturn(0);
        when(orderRepositoryPort.save(any(OrderDomain.class))).thenAnswer(inv -> inv.getArgument(0));
        when(orderMapper.toResponse(any(OrderDomain.class))).thenReturn(OrderResponse.builder().id(1L).outletId(1L).build());

        OrderResponse result = createOrderUseCase.execute(request);

        assertNotNull(result);
        assertEquals(99L, org.frias.avalon.core.tenant.TenantContext.getTenantId());
        assertEquals(77L, org.frias.avalon.core.tenant.TenantContext.getTenantOutletId());

        org.frias.avalon.core.tenant.TenantContext.clear();
    }
}
