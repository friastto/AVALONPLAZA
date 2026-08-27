package org.frias.avalon.domain.sale.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.frias.avalon.core.exeptions.GlobalExceptionHandler;
import org.frias.avalon.domain.masterdata.application.dto.response.MasterDataResponseDto;
import org.frias.avalon.domain.sale.application.dto.request.CreateExchangeRequest;
import org.frias.avalon.domain.sale.application.dto.request.CreateReturnRequest;
import org.frias.avalon.domain.sale.application.dto.request.ExchangeItemRequest;
import org.frias.avalon.domain.sale.application.dto.request.ReturnItemRequest;
import org.frias.avalon.domain.sale.application.dto.response.ExchangeResponse;
import org.frias.avalon.domain.sale.application.dto.response.ReturnItemResponse;
import org.frias.avalon.domain.sale.application.dto.response.ReturnResponse;
import org.frias.avalon.domain.sale.application.dto.response.SaleItemResponse;
import org.frias.avalon.domain.sale.application.dto.response.SaleResponse;
import org.frias.avalon.domain.sale.application.usecase.sale.returns.CreateExchangeUseCase;
import org.frias.avalon.domain.sale.application.usecase.sale.returns.CreateReturnUseCase;
import org.frias.avalon.domain.sale.application.usecase.sale.returns.FindReturnsUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
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
@DisplayName("Standalone MockMvc Unit Tests for ReturnController")
class ReturnControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private CreateReturnUseCase createReturnUseCase;

    @Mock
    private CreateExchangeUseCase createExchangeUseCase;

    @Mock
    private FindReturnsUseCase findReturnsUseCase;

    @BeforeEach
    void setUp() {
        ReturnController controller = new ReturnController(
                createReturnUseCase,
                createExchangeUseCase,
                findReturnsUseCase
        );

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private ReturnResponse buildSampleReturnResponse(UUID returnCode, UUID originalSaleCode) {
        ReturnItemResponse item = new ReturnItemResponse(100L, "Producto Test", "2 UN", new BigDecimal("25.00"), new BigDecimal("50.00"));
        return new ReturnResponse(
                1L,
                returnCode,
                originalSaleCode,
                10L,
                new BigDecimal("50.00"),
                "DEFECTO",
                "Notas",
                "REEMBOLSO",
                "DEV",
                "Juan Perez",
                "123456789",
                1L,
                5L,
                LocalDateTime.now(),
                List.of(item)
        );
    }

    @Test
    @DisplayName("POST /avalon/returns - Should create return successfully and return 201 Created")
    void shouldCreateReturnSuccessfullyAndReturn201() throws Exception {
        UUID returnCode = UUID.randomUUID();
        UUID originalSaleCode = UUID.randomUUID();

        CreateReturnRequest request = new CreateReturnRequest(
                originalSaleCode,
                "DEFECTO",
                "Notas de prueba",
                "REEMBOLSO",
                List.of(new ReturnItemRequest(100L, "2")),
                true
        );

        ReturnResponse expectedResponse = buildSampleReturnResponse(returnCode, originalSaleCode);
        given(createReturnUseCase.execute(any(CreateReturnRequest.class))).willReturn(expectedResponse);

        mockMvc.perform(post("/avalon/returns")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status", is(201)))
                .andExpect(jsonPath("$.message", is("Devolución procesada con éxito")))
                .andExpect(jsonPath("$.data.id", is(1)))
                .andExpect(jsonPath("$.data.reason", is("DEFECTO")));
    }

    @Test
    @DisplayName("POST /avalon/returns - Should return 400 Bad Request when request body is invalid")
    void shouldReturn400BadRequestWhenCreateReturnRequestIsInvalid() throws Exception {
        CreateReturnRequest invalidRequest = new CreateReturnRequest(
                null,
                "",
                "Notas",
                "REEMBOLSO",
                Collections.emptyList(),
                true
        );

        mockMvc.perform(post("/avalon/returns")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /avalon/returns/exchange - Should process exchange successfully and return 201 Created")
    void shouldProcessExchangeSuccessfullyAndReturn201() throws Exception {
        UUID returnCode = UUID.randomUUID();
        UUID saleCode = UUID.randomUUID();
        UUID originalSaleCode = UUID.randomUUID();

        CreateExchangeRequest request = new CreateExchangeRequest(
                originalSaleCode,
                "DEFECTO",
                "Notas cambio",
                List.of(new ReturnItemRequest(100L, "2")),
                List.of(new ExchangeItemRequest(101L, "1")),
                1L,
                new BigDecimal("10.00"),
                false
        );

        ReturnResponse returnResp = buildSampleReturnResponse(returnCode, originalSaleCode);

        MasterDataResponseDto paymentMethod = new MasterDataResponseDto(1L, "EFE", "Efectivo");
        MasterDataResponseDto statusDto = new MasterDataResponseDto(1L, "ACT", "Activo");
        SaleItemResponse newSaleItem = new SaleItemResponse(101L, "Producto Nuevo", "1 UN", new BigDecimal("60.00"), new BigDecimal("60.00"));

        SaleResponse newSaleResp = new SaleResponse(
                2L, saleCode, new BigDecimal("60.00"), new BigDecimal("70.00"), new BigDecimal("10.00"),
                LocalDateTime.now(), paymentMethod, statusDto, "Juan Perez", "123456789", 1L, 5L, List.of(newSaleItem)
        );

        ExchangeResponse expectedExchange = new ExchangeResponse(
                returnResp, newSaleResp, new BigDecimal("50.00"), new BigDecimal("60.00"), new BigDecimal("10.00"),
                "Excedente de $10.00 cobrado exitosamente."
        );

        given(createExchangeUseCase.execute(any(CreateExchangeRequest.class))).willReturn(expectedExchange);

        mockMvc.perform(post("/avalon/returns/exchange")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status", is(201)))
                .andExpect(jsonPath("$.message", is("Intercambio procesado con éxito")))
                .andExpect(jsonPath("$.data.netDifference", is(10.00)));
    }

    @Test
    @DisplayName("GET /avalon/returns/{returnCode} - Should return 404 Not Found when return does not exist")
    void shouldReturn404NotFoundWhenReturnCodeDoesNotExist() throws Exception {
        UUID nonExistentCode = UUID.randomUUID();
        given(findReturnsUseCase.findByCode(nonExistentCode)).willReturn(Optional.empty());

        mockMvc.perform(get("/avalon/returns/{returnCode}", nonExistentCode.toString()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)));
    }

    @Test
    @DisplayName("GET /avalon/returns - Should list returns by outlet with 200 OK")
    void shouldListReturnsByOutletSuccessfully() throws Exception {
        UUID returnCode = UUID.randomUUID();
        ReturnResponse sample = buildSampleReturnResponse(returnCode, UUID.randomUUID());
        PageImpl<ReturnResponse> page = new PageImpl<>(List.of(sample), PageRequest.of(0, 10), 1);

        given(findReturnsUseCase.findByOutlet(eq(1L), any(Pageable.class))).willReturn(page);

        mockMvc.perform(get("/avalon/returns")
                        .param("outletId", "1")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(200)))
                .andExpect(jsonPath("$.message", is("Listado de devoluciones obtenido con éxito")))
                .andExpect(jsonPath("$.data.content[0].id", is(1)));
    }
}
