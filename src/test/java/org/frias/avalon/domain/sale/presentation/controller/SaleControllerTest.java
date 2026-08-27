package org.frias.avalon.domain.sale.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.frias.avalon.core.exeptions.GlobalExceptionHandler;
import org.frias.avalon.core.exeptions.ResourceNotFoundException;
import org.frias.avalon.domain.masterdata.application.dto.response.MasterDataResponseDto;
import org.frias.avalon.domain.sale.application.dto.request.CreateSaleRequest;
import org.frias.avalon.domain.sale.application.dto.request.SaleItemRequest;
import org.frias.avalon.domain.sale.application.dto.response.SaleItemResponse;
import org.frias.avalon.domain.sale.application.dto.response.SaleResponse;
import org.frias.avalon.domain.sale.application.usecase.sale.create.CreateSaleUseCase;
import org.frias.avalon.domain.sale.application.usecase.sale.find.FindAllSalesUseCase;
import org.frias.avalon.domain.sale.application.usecase.sale.find.FindSaleByCodeUseCase;
import org.frias.avalon.domain.sale.application.usecase.sale.find.SearchSalesUseCase;
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
import org.springframework.test.web.servlet.ResultActions;
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
@DisplayName("Standalone MockMvc Unit Tests for SaleController")
class SaleControllerTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private CreateSaleUseCase createSaleUseCase;

    @Mock
    private FindSaleByCodeUseCase findSaleByCodeUseCase;

    @Mock
    private FindAllSalesUseCase findAllSalesUseCase;

    @Mock
    private SearchSalesUseCase searchSalesUseCase;

    @BeforeEach
    void setUp() {
        SaleController controller = new SaleController(
                createSaleUseCase,
                findSaleByCodeUseCase,
                findAllSalesUseCase,
                searchSalesUseCase
        );

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private SaleResponse buildSampleSaleResponse(UUID saleCode) {
        MasterDataResponseDto paymentMethod = new MasterDataResponseDto(10L, "EFECTIVO", "Efectivo");
        MasterDataResponseDto status = new MasterDataResponseDto(1L, "COMPLETADA", "Completada");
        SaleItemResponse item = new SaleItemResponse(100L, "Producto Test", "2", new BigDecimal("25.00"), new BigDecimal("50.00"));

        return new SaleResponse(
                1L,
                saleCode,
                new BigDecimal("50.00"),
                new BigDecimal("100.00"),
                new BigDecimal("50.00"),
                LocalDateTime.now(),
                paymentMethod,
                status,
                "Juan Perez",
                "123456789",
                1L,
                10L,
                List.of(item)
        );
    }

    @Test
    @DisplayName("POST /avalon/sales - Should create sale successfully and return 201 Created")
    void shouldCreateSaleSuccessfullyAndReturn201() throws Exception {
        UUID saleCode = UUID.randomUUID();
        CreateSaleRequest request = new CreateSaleRequest(
                "123456789",
                1L,
                10L,
                new BigDecimal("100.00"),
                List.of(new SaleItemRequest(100L, "2")),
                true
        );

        SaleResponse expectedResponse = buildSampleSaleResponse(saleCode);
        given(createSaleUseCase.execute(any(CreateSaleRequest.class))).willReturn(expectedResponse);

        ResultActions result = mockMvc.perform(post("/avalon/sales")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        result.andExpect(status().isCreated())
                .andExpect(jsonPath("$.status", is(201)))
                .andExpect(jsonPath("$.message", is("Venta registrada con éxito")))
                .andExpect(jsonPath("$.data.id", is(1)))
                .andExpect(jsonPath("$.data.clientNumberid", is("123456789")))
                .andExpect(jsonPath("$.data.totalAmount", is(50.00)));
    }

    @Test
    @DisplayName("POST /avalon/sales - Should return 400 Bad Request when client document is null")
    void shouldReturn400BadRequestWhenClientNumberIdIsNull() throws Exception {
        CreateSaleRequest invalidRequest = new CreateSaleRequest(
                null,
                1L,
                10L,
                new BigDecimal("100.00"),
                List.of(new SaleItemRequest(100L, "2")),
                true
        );

        mockMvc.perform(post("/avalon/sales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /avalon/sales - Should return 400 Bad Request when items list is empty")
    void shouldReturn400BadRequestWhenItemsListIsEmpty() throws Exception {
        CreateSaleRequest invalidRequest = new CreateSaleRequest(
                "123456789",
                1L,
                10L,
                new BigDecimal("100.00"),
                Collections.emptyList(),
                true
        );

        mockMvc.perform(post("/avalon/sales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /avalon/sales - Should return 400 Bad Request when outletId is null")
    void shouldReturn400BadRequestWhenOutletIdIsNull() throws Exception {
        CreateSaleRequest invalidRequest = new CreateSaleRequest(
                "123456789",
                null,
                10L,
                new BigDecimal("100.00"),
                List.of(new SaleItemRequest(100L, "2")),
                true
        );

        mockMvc.perform(post("/avalon/sales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /avalon/sales/recent - Should return recent sales with 200 OK")
    void shouldReturnRecentSalesSuccessfully() throws Exception {
        UUID saleCode = UUID.randomUUID();
        SaleResponse sample = buildSampleSaleResponse(saleCode);
        given(searchSalesUseCase.getRecentSales(1L)).willReturn(List.of(sample));

        mockMvc.perform(get("/avalon/sales/recent")
                        .param("outletId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(200)))
                .andExpect(jsonPath("$.message", is("Ventas recientes obtenidas")))
                .andExpect(jsonPath("$.data[0].id", is(1)))
                .andExpect(jsonPath("$.data[0].clientFullName", is("Juan Perez")));
    }

    @Test
    @DisplayName("GET /avalon/sales/search - Should search sales by query with 200 OK")
    void shouldSearchSalesSuccessfully() throws Exception {
        UUID saleCode = UUID.randomUUID();
        SaleResponse sample = buildSampleSaleResponse(saleCode);
        given(searchSalesUseCase.search(1L, "123456789")).willReturn(List.of(sample));

        mockMvc.perform(get("/avalon/sales/search")
                        .param("query", "123456789")
                        .param("outletId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(200)))
                .andExpect(jsonPath("$.message", is("Búsqueda realizada")))
                .andExpect(jsonPath("$.data[0].clientNumberid", is("123456789")));
    }

    @Test
    @DisplayName("GET /avalon/sales/search - Should return 400 Bad Request if missing query param")
    void shouldReturn400BadRequestWhenQueryParamIsMissing() throws Exception {
        mockMvc.perform(get("/avalon/sales/search"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /avalon/sales/{code} - Should get sale by code with 200 OK")
    void shouldGetSaleByCodeSuccessfully() throws Exception {
        UUID saleCode = UUID.randomUUID();
        String codeStr = saleCode.toString();
        SaleResponse sample = buildSampleSaleResponse(saleCode);

        given(searchSalesUseCase.findByFlexibleCode(codeStr, 1L)).willReturn(sample);

        mockMvc.perform(get("/avalon/sales/{code}", codeStr)
                        .param("outletId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(200)))
                .andExpect(jsonPath("$.message", is("Venta encontrada")))
                .andExpect(jsonPath("$.data.saleCode", is(codeStr)));
    }

    @Test
    @DisplayName("GET /avalon/sales/{code} - Should return 404 Not Found when sale code does not exist")
    void shouldReturn404NotFoundWhenSaleCodeDoesNotExist() throws Exception {
        String nonExistentCode = "NONEXISTENT";
        given(searchSalesUseCase.findByFlexibleCode(nonExistentCode, null))
                .willThrow(new ResourceNotFoundException("Venta no encontrada: " + nonExistentCode));

        mockMvc.perform(get("/avalon/sales/{code}", nonExistentCode))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.message", is("Venta no encontrada: NONEXISTENT")));
    }

    @Test
    @DisplayName("GET /avalon/sales - Should list sales with pagination 200 OK")
    void shouldListSalesWithPageableSuccessfully() throws Exception {
        UUID saleCode = UUID.randomUUID();
        SaleResponse sample = buildSampleSaleResponse(saleCode);
        PageImpl<SaleResponse> page = new PageImpl<>(List.of(sample), PageRequest.of(0, 10), 1);

        given(findAllSalesUseCase.execute(eq(1L), any(Pageable.class))).willReturn(page);

        mockMvc.perform(get("/avalon/sales")
                        .param("outletId", "1")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(200)))
                .andExpect(jsonPath("$.message", is("Listado de ventas obtenido con éxito")))
                .andExpect(jsonPath("$.data.content[0].id", is(1)));
    }
}
