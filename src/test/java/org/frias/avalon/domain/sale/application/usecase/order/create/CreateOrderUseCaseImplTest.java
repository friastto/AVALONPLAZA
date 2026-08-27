package org.frias.avalon.domain.sale.application.usecase.order.create;

import org.frias.avalon.core.exeptions.BusinessException;
import org.frias.avalon.core.exeptions.DomainValidationException;
import org.frias.avalon.core.exeptions.ResourceNotFoundException;
import org.frias.avalon.core.permissions.CurrentUserProviderPort;
import org.frias.avalon.domain.masterdata.domain.model.MasterRoot;
import org.frias.avalon.domain.masterdata.domain.model.MasterTree;
import org.frias.avalon.domain.masterdata.domain.repository.MasterDataRepositoryPort;
import org.frias.avalon.domain.masterdata.domain.service.MasterTreeProvider;
import org.frias.avalon.domain.product.application.port.ProductOutletRepositoryPort;
import org.frias.avalon.domain.product.domain.ProductDomain;
import org.frias.avalon.domain.sale.application.dto.request.CreateOrderRequest;
import org.frias.avalon.domain.sale.application.dto.request.OrderItemRequest;
import org.frias.avalon.domain.sale.application.dto.response.OrderResponse;
import org.frias.avalon.domain.sale.application.port.OrderRepositoryPort;
import org.frias.avalon.domain.sale.domain.OrderDomain;
import org.frias.avalon.domain.sale.domain.service.SaleWeightConversionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Unit Tests for CreateOrderUseCaseImpl")
class CreateOrderUseCaseImplTest {

    @Mock
    private OrderRepositoryPort orderRepositoryPort;

    @Mock
    private ProductOutletRepositoryPort productOutletRepositoryPort;

    @Mock
    private MasterDataRepositoryPort masterDataRepositoryPort;

    @Mock
    private MasterTreeProvider masterTreeProvider;

    @Mock
    private SaleWeightConversionService weightConversionService;

    @Mock
    private CurrentUserProviderPort currentUserProvider;

    private CreateOrderUseCaseImpl createOrderUseCase;

    @BeforeEach
    void setUp() {
        createOrderUseCase = new CreateOrderUseCaseImpl(
                orderRepositoryPort,
                productOutletRepositoryPort,
                masterDataRepositoryPort,
                masterTreeProvider,
                weightConversionService,
                currentUserProvider
        );
    }

    @Test
    @DisplayName("Should throw BusinessException when user is not admin and outlet context is null")
    void shouldThrowBusinessExceptionWhenTenantOutletIsNull() {
        CreateOrderRequest request = new CreateOrderRequest(1L, 10L, List.of(new OrderItemRequest(100L, "2")));

        when(currentUserProvider.hasRole("ROLE_ADMIN")).thenReturn(false);
        when(currentUserProvider.hasRole("ROLE_ADMINTI")).thenReturn(false);
        when(currentUserProvider.getCurrentOutletId()).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class, () -> createOrderUseCase.execute(request));
        assertEquals("No se detectó una tienda asociada en el contexto del empleado actual.", ex.getMessage());
    }

    @Test
    @DisplayName("Should throw BusinessException when user is not admin and tenant outlet ID mismatch")
    void shouldThrowBusinessExceptionWhenTenantOutletMismatch() {
        CreateOrderRequest request = new CreateOrderRequest(1L, 10L, List.of(new OrderItemRequest(100L, "2")));

        when(currentUserProvider.hasRole("ROLE_ADMIN")).thenReturn(false);
        when(currentUserProvider.hasRole("ROLE_ADMINTI")).thenReturn(false);
        when(currentUserProvider.getCurrentOutletId()).thenReturn(5L);

        BusinessException ex = assertThrows(BusinessException.class, () -> createOrderUseCase.execute(request));
        assertEquals("Acceso denegado: No tienes permisos para registrar pedidos en otra tienda.", ex.getMessage());
    }

    @Test
    @DisplayName("Should throw IllegalStateException when pending status PEN is missing in MasterData")
    void shouldThrowIllegalStateExceptionWhenPendingStatusMissing() {
        CreateOrderRequest request = new CreateOrderRequest(1L, 10L, List.of(new OrderItemRequest(100L, "2")));

        when(currentUserProvider.hasRole("ROLE_ADMIN")).thenReturn(true);
        when(masterDataRepositoryPort.getIdByCode("PEN")).thenReturn(null);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> createOrderUseCase.execute(request));
        assertEquals("Estado Pendiente ('PEN') no encontrado en MasterData.", ex.getMessage());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when product does not exist")
    void shouldThrowResourceNotFoundExceptionWhenProductDoesNotExist() {
        CreateOrderRequest request = new CreateOrderRequest(1L, 10L, List.of(new OrderItemRequest(100L, "2")));

        when(currentUserProvider.hasRole("ROLE_ADMIN")).thenReturn(true);
        when(masterDataRepositoryPort.getIdByCode("PEN")).thenReturn(500L);
        when(masterTreeProvider.getTree()).thenReturn(new MasterTree(List.of()));
        when(productOutletRepositoryPort.findById(100L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () -> createOrderUseCase.execute(request));
        assertEquals("El producto con ID 100 no existe", ex.getMessage());
    }

    @Test
    @DisplayName("Should throw BusinessException when product belongs to another outlet")
    void shouldThrowBusinessExceptionWhenProductBelongsToAnotherOutlet() {
        CreateOrderRequest request = new CreateOrderRequest(1L, 10L, List.of(new OrderItemRequest(100L, "2")));
        ProductDomain product = ProductDomain.fromPersistence(
                100L, "Carne", "Carne", 50, 2L, null, new BigDecimal("20.00"), 99L, 1L, LocalDateTime.now(), LocalDateTime.now()
        );

        when(currentUserProvider.hasRole("ROLE_ADMIN")).thenReturn(true);
        when(masterDataRepositoryPort.getIdByCode("PEN")).thenReturn(500L);
        when(masterTreeProvider.getTree()).thenReturn(new MasterTree(List.of()));
        when(productOutletRepositoryPort.findById(100L)).thenReturn(Optional.of(product));

        BusinessException ex = assertThrows(BusinessException.class, () -> createOrderUseCase.execute(request));
        assertEquals("El producto 'Carne' no pertenece a la tienda del pedido.", ex.getMessage());
    }

    @Test
    @DisplayName("Should throw DomainValidationException when product unit measure node is null in MasterTree")
    void shouldThrowDomainValidationExceptionWhenUnitNodeNotFound() {
        CreateOrderRequest request = new CreateOrderRequest(1L, 10L, List.of(new OrderItemRequest(100L, "2")));
        ProductDomain product = ProductDomain.fromPersistence(
                100L, "Manzana", "Manzana", 50, 99L, null, new BigDecimal("5.00"), 10L, 1L, LocalDateTime.now(), LocalDateTime.now()
        );

        when(currentUserProvider.hasRole("ROLE_ADMIN")).thenReturn(true);
        when(masterDataRepositoryPort.getIdByCode("PEN")).thenReturn(500L);
        when(masterTreeProvider.getTree()).thenReturn(new MasterTree(List.of()));
        when(productOutletRepositoryPort.findById(100L)).thenReturn(Optional.of(product));

        DomainValidationException ex = assertThrows(DomainValidationException.class, () -> createOrderUseCase.execute(request));
        assertEquals("La unidad de medida del producto Manzana no es válida.", ex.getMessage());
    }

    @Test
    @DisplayName("Should throw BusinessException when weighable product has invalid quantity format")
    void shouldThrowBusinessExceptionWhenWeighableQuantityInvalid() {
        CreateOrderRequest request = new CreateOrderRequest(1L, 10L, List.of(new OrderItemRequest(100L, "abc")));
        ProductDomain product = ProductDomain.fromPersistence(
                100L, "Queso", "Queso", 50, 2L, null, new BigDecimal("30.00"), 10L, 1L, LocalDateTime.now(), LocalDateTime.now()
        );
        MasterRoot unitNode = MasterRoot.fromPersistence(2L, "KG", "Kilogramos", null, 1L);

        when(currentUserProvider.hasRole("ROLE_ADMIN")).thenReturn(true);
        when(masterDataRepositoryPort.getIdByCode("PEN")).thenReturn(500L);
        when(masterTreeProvider.getTree()).thenReturn(new MasterTree(List.of(unitNode)));
        when(productOutletRepositoryPort.findById(100L)).thenReturn(Optional.of(product));
        when(weightConversionService.isWeighable("KG")).thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class, () -> createOrderUseCase.execute(request));
        assertEquals("La cantidad 'abc' no es un decimal válido para el producto: Queso", ex.getMessage());
    }

    @Test
    @DisplayName("Should throw BusinessException when non-weighable product has invalid integer quantity")
    void shouldThrowBusinessExceptionWhenNonWeighableQuantityInvalid() {
        CreateOrderRequest request = new CreateOrderRequest(1L, 10L, List.of(new OrderItemRequest(100L, "2.5")));
        ProductDomain product = ProductDomain.fromPersistence(
                100L, "Soda", "Soda", 50, 3L, null, new BigDecimal("2.50"), 10L, 1L, LocalDateTime.now(), LocalDateTime.now()
        );
        MasterRoot unitNode = MasterRoot.fromPersistence(3L, "UND", "Unidades", null, 1L);

        when(currentUserProvider.hasRole("ROLE_ADMIN")).thenReturn(true);
        when(masterDataRepositoryPort.getIdByCode("PEN")).thenReturn(500L);
        when(masterTreeProvider.getTree()).thenReturn(new MasterTree(List.of(unitNode)));
        when(productOutletRepositoryPort.findById(100L)).thenReturn(Optional.of(product));
        when(weightConversionService.isWeighable("UND")).thenReturn(false);

        BusinessException ex = assertThrows(BusinessException.class, () -> createOrderUseCase.execute(request));
        assertEquals("La cantidad '2.5' debe ser un entero para el producto: Soda", ex.getMessage());
    }

    @Test
    @DisplayName("Should throw BusinessException when quantity in base units is zero or negative")
    void shouldThrowBusinessExceptionWhenQtyInBaseUnitsIsZeroOrNegative() {
        CreateOrderRequest request = new CreateOrderRequest(1L, 10L, List.of(new OrderItemRequest(100L, "0")));
        ProductDomain product = ProductDomain.fromPersistence(
                100L, "Pan", "Pan", 50, 3L, null, new BigDecimal("1.00"), 10L, 1L, LocalDateTime.now(), LocalDateTime.now()
        );
        MasterRoot unitNode = MasterRoot.fromPersistence(3L, "UND", "Unidades", null, 1L);

        when(currentUserProvider.hasRole("ROLE_ADMIN")).thenReturn(true);
        when(masterDataRepositoryPort.getIdByCode("PEN")).thenReturn(500L);
        when(masterTreeProvider.getTree()).thenReturn(new MasterTree(List.of(unitNode)));
        when(productOutletRepositoryPort.findById(100L)).thenReturn(Optional.of(product));
        when(weightConversionService.isWeighable("UND")).thenReturn(false);

        BusinessException ex = assertThrows(BusinessException.class, () -> createOrderUseCase.execute(request));
        assertEquals("La cantidad para el producto Pan debe ser mayor a cero.", ex.getMessage());
    }

    @Test
    @DisplayName("Should create order successfully for weighable product with KG or L unit")
    void shouldCreateOrderSuccessfullyForKgUnit() {
        Long paymentMethodId = 1L;
        Long outletId = 10L;
        Long pendingStatusId = 500L;
        CreateOrderRequest request = new CreateOrderRequest(paymentMethodId, outletId, List.of(new OrderItemRequest(100L, "1,500")));

        ProductDomain product = ProductDomain.fromPersistence(
                100L, "Carne Molida", "Carne", 10000, 2L, null, new BigDecimal("20.00"), outletId, 1L, LocalDateTime.now(), LocalDateTime.now()
        );
        MasterRoot unitNode = MasterRoot.fromPersistence(2L, "KG", "Kilogramos", null, 1L);
        MasterRoot payNode = MasterRoot.fromPersistence(paymentMethodId, "TARJETA", "Tarjeta de Crédito", null, 1L);
        MasterRoot statusNode = MasterRoot.fromPersistence(pendingStatusId, "PEN", "Pendiente", null, 1L);

        MasterTree masterTree = new MasterTree(List.of(unitNode, payNode, statusNode));

        when(currentUserProvider.hasRole("ROLE_ADMIN")).thenReturn(false);
        when(currentUserProvider.hasRole("ROLE_ADMINTI")).thenReturn(false);
        when(currentUserProvider.getCurrentOutletId()).thenReturn(outletId);
        when(masterDataRepositoryPort.getIdByCode("PEN")).thenReturn(pendingStatusId);
        when(masterTreeProvider.getTree()).thenReturn(masterTree);
        when(productOutletRepositoryPort.findById(100L)).thenReturn(Optional.of(product));
        when(weightConversionService.isWeighable("KG")).thenReturn(true);
        when(weightConversionService.convertToBaseUnit(new BigDecimal("1.500"), "KG")).thenReturn(1500);
        when(weightConversionService.formatFromBaseUnit(1500, "KG")).thenReturn("1.500 KG");

        OrderDomain savedDomain = OrderDomain.fromPersistence(
                99L, UUID.randomUUID(), new BigDecimal("30.00"), paymentMethodId, pendingStatusId, outletId,
                LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), List.of()
        );
        when(orderRepositoryPort.save(any(OrderDomain.class))).thenReturn(savedDomain);

        OrderResponse response = createOrderUseCase.execute(request);

        assertNotNull(response);
        assertEquals(99L, response.id());
        assertEquals(outletId, response.outletId());
        assertEquals(new BigDecimal("30.00"), response.totalAmount());
        assertEquals("TARJETA", response.paymentMethod().shortName());
        assertEquals("PEN", response.status().shortName());
        assertEquals(1, response.items().size());
        assertEquals("1.500 KG", response.items().get(0).displayQuantity());
        assertEquals(new BigDecimal("30.00"), response.items().get(0).subtotal());

        ArgumentCaptor<OrderDomain> orderCaptor = ArgumentCaptor.forClass(OrderDomain.class);
        verify(orderRepositoryPort).save(orderCaptor.capture());
        OrderDomain captured = orderCaptor.getValue();
        assertEquals(paymentMethodId, captured.getPaymentMethodId());
        assertEquals(pendingStatusId, captured.getStatusId());
        assertEquals(outletId, captured.getOutletId());
    }

    @Test
    @DisplayName("Should create order successfully for weighable product with LB unit")
    void shouldCreateOrderSuccessfullyForLbUnit() {
        Long paymentMethodId = 1L;
        Long outletId = 10L;
        Long pendingStatusId = 500L;
        CreateOrderRequest request = new CreateOrderRequest(paymentMethodId, outletId, List.of(new OrderItemRequest(100L, "1.0")));

        ProductDomain product = ProductDomain.fromPersistence(
                100L, "Pollo", "Pollo", 10000, 4L, null, new BigDecimal("10.00"), outletId, 1L, LocalDateTime.now(), LocalDateTime.now()
        );
        MasterRoot unitNode = MasterRoot.fromPersistence(4L, "LB", "Libras", null, 1L);
        MasterRoot payNode = MasterRoot.fromPersistence(paymentMethodId, "EFE", "Efectivo", null, 1L);
        MasterRoot statusNode = MasterRoot.fromPersistence(pendingStatusId, "PEN", "Pendiente", null, 1L);

        MasterTree masterTree = new MasterTree(List.of(unitNode, payNode, statusNode));

        when(currentUserProvider.hasRole("ROLE_ADMIN")).thenReturn(true);
        when(masterDataRepositoryPort.getIdByCode("PEN")).thenReturn(pendingStatusId);
        when(masterTreeProvider.getTree()).thenReturn(masterTree);
        when(productOutletRepositoryPort.findById(100L)).thenReturn(Optional.of(product));
        when(weightConversionService.isWeighable("LB")).thenReturn(true);
        when(weightConversionService.convertToBaseUnit(new BigDecimal("1.0"), "LB")).thenReturn(454);
        when(weightConversionService.formatFromBaseUnit(454, "LB")).thenReturn("1.000 LB");

        OrderDomain savedDomain = OrderDomain.fromPersistence(
                101L, UUID.randomUUID(), new BigDecimal("10.01"), paymentMethodId, pendingStatusId, outletId,
                LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), List.of()
        );
        when(orderRepositoryPort.save(any(OrderDomain.class))).thenReturn(savedDomain);

        OrderResponse response = createOrderUseCase.execute(request);

        assertNotNull(response);
        assertEquals(1, response.items().size());
        assertEquals("1.000 LB", response.items().get(0).displayQuantity());

        ArgumentCaptor<OrderDomain> orderCaptor = ArgumentCaptor.forClass(OrderDomain.class);
        verify(orderRepositoryPort).save(orderCaptor.capture());
        OrderDomain captured = orderCaptor.getValue();
        assertEquals(1, captured.getItems().size());
    }

    @Test
    @DisplayName("Should create order successfully for weighable product with default factor unit (e.g. GR)")
    void shouldCreateOrderSuccessfullyForWeighableDefaultFactorUnit() {
        Long paymentMethodId = 1L;
        Long outletId = 10L;
        Long pendingStatusId = 500L;
        CreateOrderRequest request = new CreateOrderRequest(paymentMethodId, outletId, List.of(new OrderItemRequest(100L, "500")));

        ProductDomain product = ProductDomain.fromPersistence(
                100L, "Especias", "Especias", 10000, 5L, null, new BigDecimal("0.05"), outletId, 1L, LocalDateTime.now(), LocalDateTime.now()
        );
        MasterRoot unitNode = MasterRoot.fromPersistence(5L, "GR", "Gramos", null, 1L);
        MasterRoot payNode = MasterRoot.fromPersistence(paymentMethodId, "EFE", "Efectivo", null, 1L);
        MasterRoot statusNode = MasterRoot.fromPersistence(pendingStatusId, "PEN", "Pendiente", null, 1L);

        MasterTree masterTree = new MasterTree(List.of(unitNode, payNode, statusNode));

        when(currentUserProvider.hasRole("ROLE_ADMIN")).thenReturn(true);
        when(masterDataRepositoryPort.getIdByCode("PEN")).thenReturn(pendingStatusId);
        when(masterTreeProvider.getTree()).thenReturn(masterTree);
        when(productOutletRepositoryPort.findById(100L)).thenReturn(Optional.of(product));
        when(weightConversionService.isWeighable("GR")).thenReturn(true);
        when(weightConversionService.convertToBaseUnit(new BigDecimal("500"), "GR")).thenReturn(500);
        when(weightConversionService.formatFromBaseUnit(500, "GR")).thenReturn("500 GR");

        OrderDomain savedDomain = OrderDomain.fromPersistence(
                102L, UUID.randomUUID(), new BigDecimal("25.00"), paymentMethodId, pendingStatusId, outletId,
                LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), List.of()
        );
        when(orderRepositoryPort.save(any(OrderDomain.class))).thenReturn(savedDomain);

        OrderResponse response = createOrderUseCase.execute(request);

        assertNotNull(response);
        assertEquals(1, response.items().size());
        assertEquals(new BigDecimal("25.00"), response.items().get(0).subtotal());
    }

    @Test
    @DisplayName("Should create order successfully for non-weighable product with L unit measure code")
    void shouldCreateOrderSuccessfullyForLitreWeighableProduct() {
        Long paymentMethodId = 1L;
        Long outletId = 10L;
        Long pendingStatusId = 500L;
        CreateOrderRequest request = new CreateOrderRequest(paymentMethodId, outletId, List.of(new OrderItemRequest(100L, "2")));

        ProductDomain product = ProductDomain.fromPersistence(
                100L, "Leche", "Leche 1L", 100, 6L, null, new BigDecimal("12.00"), outletId, 1L, LocalDateTime.now(), LocalDateTime.now()
        );
        MasterRoot unitNode = MasterRoot.fromPersistence(6L, "L", "Litros", null, 1L);
        MasterRoot payNode = MasterRoot.fromPersistence(paymentMethodId, "EFE", "Efectivo", null, 1L);
        MasterRoot statusNode = MasterRoot.fromPersistence(pendingStatusId, "PEN", "Pendiente", null, 1L);

        MasterTree masterTree = new MasterTree(List.of(unitNode, payNode, statusNode));

        when(currentUserProvider.hasRole("ROLE_ADMIN")).thenReturn(true);
        when(masterDataRepositoryPort.getIdByCode("PEN")).thenReturn(pendingStatusId);
        when(masterTreeProvider.getTree()).thenReturn(masterTree);
        when(productOutletRepositoryPort.findById(100L)).thenReturn(Optional.of(product));
        when(weightConversionService.isWeighable("L")).thenReturn(true);
        when(weightConversionService.convertToBaseUnit(new BigDecimal("2"), "L")).thenReturn(2000);
        when(weightConversionService.formatFromBaseUnit(2000, "L")).thenReturn("2.000 L");

        OrderDomain savedDomain = OrderDomain.fromPersistence(
                103L, UUID.randomUUID(), new BigDecimal("24.00"), paymentMethodId, pendingStatusId, outletId,
                LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), List.of()
        );
        when(orderRepositoryPort.save(any(OrderDomain.class))).thenReturn(savedDomain);

        OrderResponse response = createOrderUseCase.execute(request);

        assertNotNull(response);
        assertEquals(1, response.items().size());
        assertEquals(new BigDecimal("24.00"), response.items().get(0).subtotal());
    }

    @Test
    @DisplayName("Should create order successfully for non-weighable product (units)")
    void shouldCreateOrderSuccessfullyForNonWeighableProduct() {
        Long paymentMethodId = 1L;
        Long outletId = 10L;
        Long pendingStatusId = 500L;
        CreateOrderRequest request = new CreateOrderRequest(paymentMethodId, outletId, List.of(new OrderItemRequest(100L, "3")));

        ProductDomain product = ProductDomain.fromPersistence(
                100L, "Galletas", "Galletas", 100, 3L, null, new BigDecimal("15.00"), outletId, 1L, LocalDateTime.now(), LocalDateTime.now()
        );
        MasterRoot unitNode = MasterRoot.fromPersistence(3L, "UND", "Unidades", null, 1L);
        MasterRoot payNode = MasterRoot.fromPersistence(paymentMethodId, "EFE", "Efectivo", null, 1L);
        MasterRoot statusNode = MasterRoot.fromPersistence(pendingStatusId, "PEN", "Pendiente", null, 1L);

        MasterTree masterTree = new MasterTree(List.of(unitNode, payNode, statusNode));

        when(currentUserProvider.hasRole("ROLE_ADMIN")).thenReturn(true);
        when(masterDataRepositoryPort.getIdByCode("PEN")).thenReturn(pendingStatusId);
        when(masterTreeProvider.getTree()).thenReturn(masterTree);
        when(productOutletRepositoryPort.findById(100L)).thenReturn(Optional.of(product));
        when(weightConversionService.isWeighable("UND")).thenReturn(false);
        when(weightConversionService.formatFromBaseUnit(3, "UND")).thenReturn("3 UND");

        OrderDomain savedDomain = OrderDomain.fromPersistence(
                104L, UUID.randomUUID(), new BigDecimal("45.00"), paymentMethodId, pendingStatusId, outletId,
                LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), List.of()
        );
        when(orderRepositoryPort.save(any(OrderDomain.class))).thenReturn(savedDomain);

        OrderResponse response = createOrderUseCase.execute(request);

        assertNotNull(response);
        assertEquals(104L, response.id());
        assertEquals(new BigDecimal("45.00"), response.totalAmount());
        assertEquals(1, response.items().size());
        assertEquals("3 UND", response.items().get(0).displayQuantity());
        assertEquals(new BigDecimal("45.00"), response.items().get(0).subtotal());
    }
}
