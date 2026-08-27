package org.frias.avalon.domain.sale.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.frias.avalon.core.exeptions.GlobalExceptionHandler;
import org.frias.avalon.core.exeptions.ResourceNotFoundException;
import org.frias.avalon.domain.masterdata.application.dto.response.MasterDataResponseDto;
import org.frias.avalon.domain.sale.application.dto.request.CreateOrderRequest;
import org.frias.avalon.domain.sale.application.dto.request.OrderItemRequest;
import org.frias.avalon.domain.sale.application.dto.response.OrderItemResponse;
import org.frias.avalon.domain.sale.application.dto.response.OrderResponse;
import org.frias.avalon.domain.sale.application.dto.response.SaleItemResponse;
import org.frias.avalon.domain.sale.application.dto.response.SaleResponse;
import org.frias.avalon.domain.sale.application.usecase.order.create.CreateOrderUseCase;
import org.frias.avalon.domain.sale.application.usecase.order.find.FindOrderByCodeUseCase;
import org.frias.avalon.domain.sale.application.usecase.order.invoice.InvoiceOrderUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("Standalone MockMvc Unit Tests for OrderController")
class OrderControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private CreateOrderUseCase createOrderUseCase;

    @Mock
    private FindOrderByCodeUseCase findOrderByCodeUseCase;

    @Mock
    private InvoiceOrderUseCase invoiceOrderUseCase;

    @BeforeEach
    void setUp() {
        OrderController controller = new OrderController(
                createOrderUseCase,
                findOrderByCodeUseCase,
                invoiceOrderUseCase
        );

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private OrderResponse buildSampleOrderResponse(UUID orderCode) {
        MasterDataResponseDto paymentMethod = new MasterDataResponseDto(10L, "EFECTIVO", "Efectivo");
        MasterDataResponseDto status = new MasterDataResponseDto(1L, "PEN", "Pendiente");
        OrderItemResponse item = new OrderItemResponse(100L, "Producto Test", "2 UN", new BigDecimal("25.00"), new BigDecimal("50.00"));

        return new OrderResponse(
                1L,
                orderCode,
                new BigDecimal("50.00"),
                LocalDateTime.now(),
                paymentMethod,
                status,
                1L,
                List.of(item)
        );
    }

    @Test
    @DisplayName("POST /avalon/orders - Should create order successfully and return 201 Created")
    void shouldCreateOrderSuccessfullyAndReturn201() throws Exception {
        UUID orderCode = UUID.randomUUID();
        CreateOrderRequest request = new CreateOrderRequest(
                10L,
                1L,
                List.of(new OrderItemRequest(100L, "2"))
        );

        OrderResponse expectedResponse = buildSampleOrderResponse(orderCode);
        given(createOrderUseCase.execute(any(CreateOrderRequest.class))).willReturn(expectedResponse);

        mockMvc.perform(post("/avalon/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status", is(201)))
                .andExpect(jsonPath("$.message", is("Pedido creado con éxito")))
                .andExpect(jsonPath("$.data.id", is(1)));
    }

    @Test
    @DisplayName("POST /avalon/orders - Should return 400 Bad Request when request body is invalid")
    void shouldReturn400BadRequestWhenCreateOrderRequestIsInvalid() throws Exception {
        CreateOrderRequest invalidRequest = new CreateOrderRequest(
                null,
                1L,
                Collections.emptyList()
        );

        mockMvc.perform(post("/avalon/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /avalon/orders/{code} - Should get order by code with 200 OK")
    void shouldGetOrderByCodeSuccessfully() throws Exception {
        UUID orderCode = UUID.randomUUID();
        String codeStr = orderCode.toString();
        OrderResponse sample = buildSampleOrderResponse(orderCode);

        given(findOrderByCodeUseCase.execute(orderCode)).willReturn(sample);

        mockMvc.perform(get("/avalon/orders/{code}", codeStr))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(200)))
                .andExpect(jsonPath("$.message", is("Pedido encontrado")))
                .andExpect(jsonPath("$.data.orderCode", is(codeStr)));
    }

    @Test
    @DisplayName("GET /avalon/orders/{code} - Should return 404 Not Found when order does not exist")
    void shouldReturn404NotFoundWhenOrderCodeDoesNotExist() throws Exception {
        UUID nonExistentCode = UUID.randomUUID();
        given(findOrderByCodeUseCase.execute(nonExistentCode))
                .willThrow(new ResourceNotFoundException("Pedido no encontrado: " + nonExistentCode));

        mockMvc.perform(get("/avalon/orders/{code}", nonExistentCode.toString()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)));
    }

    @Test
    @DisplayName("POST /avalon/orders/{code}/invoice - Should invoice order successfully and return 201 Created")
    void shouldInvoiceOrderSuccessfullyAndReturn201() throws Exception {
        UUID saleCode = UUID.randomUUID();
        UUID orderCode = UUID.randomUUID();

        MasterDataResponseDto paymentMethod = new MasterDataResponseDto(1L, "EFE", "Efectivo");
        MasterDataResponseDto statusDto = new MasterDataResponseDto(1L, "ACT", "Activo");
        SaleItemResponse item = new SaleItemResponse(100L, "Producto Test", "2 UN", new BigDecimal("25.00"), new BigDecimal("50.00"));

        SaleResponse expectedSale = new SaleResponse(
                10L, saleCode, new BigDecimal("50.00"), new BigDecimal("50.00"), BigDecimal.ZERO,
                LocalDateTime.now(), paymentMethod, statusDto, "Juan Perez", "123456789", 1L, 5L, List.of(item)
        );

        given(invoiceOrderUseCase.execute(eq(orderCode), eq("123456789"), any())).willReturn(expectedSale);

        mockMvc.perform(post("/avalon/orders/{code}/invoice", orderCode.toString())
                        .param("clientNumberid", "123456789"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status", is(201)))
                .andExpect(jsonPath("$.message", is("Pedido facturado y venta registrada con éxito")))
                .andExpect(jsonPath("$.data.id", is(10)));
    }
}
