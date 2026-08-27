package org.frias.avalon.domain.claim.application.usecase;

import org.frias.avalon.core.exeptions.ResourceNotFoundException;
import org.frias.avalon.domain.claim.application.dto.request.ClaimItemRequest;
import org.frias.avalon.domain.claim.application.dto.request.CreateOrderClaimRequest;
import org.frias.avalon.domain.claim.application.dto.response.ClaimResponse;
import org.frias.avalon.domain.claim.application.port.ClaimRepositoryPort;
import org.frias.avalon.domain.claim.domain.OrderClaimDomain;
import org.frias.avalon.domain.claim.infrastructure.persistence.mapper.ClaimMapper;
import org.frias.avalon.domain.masterdata.domain.repository.MasterDataRepositoryPort;
import org.frias.avalon.domain.order.application.port.OrderRepositoryPort;
import org.frias.avalon.domain.order.domain.OrderDomain;
import org.frias.avalon.domain.order.presentation.controller.OrderWebSocketController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@DisplayName("Unit Tests for CreateOrderClaimUseCaseImpl in PQRS Claim Domain")
class CreateOrderClaimUseCaseImplTest {

    private ClaimRepositoryPort claimRepositoryPort;
    private OrderRepositoryPort orderRepositoryPort;
    private MasterDataRepositoryPort masterDataRepositoryPort;
    private ClaimMapper claimMapper;
    private OrderWebSocketController orderWebSocketController;

    private CreateOrderClaimUseCaseImpl createOrderClaimUseCase;

    @BeforeEach
    void setUp() {
        claimRepositoryPort = mock(ClaimRepositoryPort.class);
        orderRepositoryPort = mock(OrderRepositoryPort.class);
        masterDataRepositoryPort = mock(MasterDataRepositoryPort.class);
        claimMapper = mock(ClaimMapper.class);
        orderWebSocketController = mock(OrderWebSocketController.class);

        createOrderClaimUseCase = new CreateOrderClaimUseCaseImpl(
                claimRepositoryPort,
                orderRepositoryPort,
                masterDataRepositoryPort,
                claimMapper,
                orderWebSocketController
        );
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when order does not exist")
    void shouldThrowExceptionWhenOrderNotFound() {
        CreateOrderClaimRequest request = new CreateOrderClaimRequest();
        request.setOrderId(99L);

        when(orderRepositoryPort.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> createOrderClaimUseCase.execute(request));
    }

    @Test
    @DisplayName("Should create claim and broadcast via WebSocket successfully")
    void shouldCreateClaimSuccessfully() {
        ClaimItemRequest itemReq = new ClaimItemRequest();
        itemReq.setOrderItemId(10L);
        itemReq.setQuantityAffected(1);
        itemReq.setReason("Producto dañado en empaque");

        CreateOrderClaimRequest request = new CreateOrderClaimRequest();
        request.setOrderId(50L);
        request.setCustomerId(5L);
        request.setClaimTypeId(2L);
        request.setDescription("El producto llegó roto");
        request.setItems(List.of(itemReq));
        request.setPhotoUrls(List.of("https://cdn.avalon.com/claims/photo1.jpg"));

        OrderDomain order = OrderDomain.builder().id(50L).build();
        when(orderRepositoryPort.findById(50L)).thenReturn(Optional.of(order));
        when(masterDataRepositoryPort.getIdByCode("CLM_PEN")).thenReturn(201L);

        OrderClaimDomain savedDomain = OrderClaimDomain.builder()
                .id(1L)
                .orderId(50L)
                .customerId(5L)
                .description("El producto llegó roto")
                .build();

        when(claimRepositoryPort.save(any(OrderClaimDomain.class))).thenReturn(savedDomain);

        ClaimResponse response = ClaimResponse.builder()
                .id(1L)
                .orderId(50L)
                .description("El producto llegó roto")
                .build();

        when(claimMapper.toResponse(savedDomain)).thenReturn(response);

        ClaimResponse result = createOrderClaimUseCase.execute(request);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(50L, result.getOrderId());
        verify(claimRepositoryPort, times(1)).save(any(OrderClaimDomain.class));
        verify(orderWebSocketController, times(1)).broadcastClaimCreated(eq(50L), eq(response));
    }
}
