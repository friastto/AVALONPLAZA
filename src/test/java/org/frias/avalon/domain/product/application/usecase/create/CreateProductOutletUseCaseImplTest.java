package org.frias.avalon.domain.product.application.usecase.create;

import org.frias.avalon.core.exeptions.DomainValidationException;
import org.frias.avalon.core.permissions.CurrentUserProviderPort;
import org.frias.avalon.domain.masterdata.domain.model.MasterRoot;
import org.frias.avalon.domain.masterdata.domain.model.MasterTree;
import org.frias.avalon.domain.masterdata.domain.service.MasterTreeProvider;
import org.frias.avalon.domain.outlet.domain.model.OutletDomain;
import org.frias.avalon.domain.outlet.domain.port.OutletRepositoryPort;
import org.frias.avalon.domain.product.application.dto.request.ProductNewDataRequest;
import org.frias.avalon.domain.product.application.dto.response.ProductResponse;
import org.frias.avalon.domain.product.application.port.ProductOutletRepositoryPort;
import org.frias.avalon.domain.product.application.service.QuantityParserService;
import org.frias.avalon.domain.product.domain.ProductDomain;
import org.frias.avalon.domain.product.domain.repository.BarcodeRepositoryPort;
import org.frias.avalon.domain.product.domain.service.UnitConversionService;
import org.frias.avalon.domain.product.infraestructure.mapper.ProductOutletMapper;
import org.frias.avalon.domain.product.presentation.ProductWebSocketPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas Unitarias para CreateProductOutletUseCase")
class CreateProductOutletUseCaseImplTest {

    @Mock
    private ProductOutletRepositoryPort productOutletRepositoryPort;
    @Mock
    private ProductOutletMapper productOutletMapper;
    @Mock
    private UnitConversionService unitConversionService;
    @Mock
    private MasterTreeProvider masterTreeProvider;
    @Mock
    private MasterTree masterTree;
    @Mock
    private QuantityParserService quantityParserService;
    @Mock
    private BarcodeRepositoryPort barcodeRepositoryPort;
    @Mock
    private CurrentUserProviderPort currentUserProvider;
    @Mock
    private ProductWebSocketPublisher productWebSocketPublisher;
    @Mock
    private OutletRepositoryPort outletRepositoryPort;
    @Mock
    private PlatformTransactionManager transactionManager;

    private CreateProductOutletUseCaseImpl createProductUseCase;

    private ProductNewDataRequest validRequestDto;
    private MasterRoot mockUnitNode;
    private MasterRoot mockActiveNode;

    @BeforeEach
    void setUp() {
        TransactionStatus transactionStatus = mock(TransactionStatus.class);
        lenient().when(transactionManager.getTransaction(any())).thenReturn(transactionStatus);

        OutletDomain mockOutlet = mock(OutletDomain.class);
        lenient().when(mockOutlet.getId()).thenReturn(100L);
        lenient().when(mockOutlet.getCompanyId()).thenReturn(10L);
        lenient().when(outletRepositoryPort.findById(any())).thenReturn(Optional.of(mockOutlet));

        lenient().when(currentUserProvider.hasRole(anyString())).thenReturn(true);
        lenient().when(barcodeRepositoryPort.findByCode(any())).thenReturn(Optional.empty());

        mockActiveNode = new MasterRoot(1L, "ACT", "Activo", null, 1L);
        lenient().when(masterTreeProvider.getTree()).thenReturn(masterTree);
        lenient().when(masterTree.getByCode("ACT")).thenReturn(mockActiveNode);

        validRequestDto = new ProductNewDataRequest(
                "",
                "Test Product",
                "Description",
                "1.3",
                50L,
                "url",
                new BigDecimal("10.0"),
                100L
        );

        mockUnitNode = new MasterRoot(50L, "KG", "Kilogramos", 10L, 1L);

        createProductUseCase = new CreateProductOutletUseCaseImpl(
                productOutletRepositoryPort,
                productOutletMapper,
                unitConversionService,
                masterTreeProvider,
                quantityParserService,
                barcodeRepositoryPort,
                currentUserProvider,
                productWebSocketPublisher,
                outletRepositoryPort,
                transactionManager
        );
    }

    @Test
    @DisplayName("Deberia crear un producto exitosamente tras parsear y validar la unidad")
    void shouldCreateProductSuccessfully() {
        Integer convertedStock = 1500;
        BigDecimal parsedQuantity = new BigDecimal("1.5");
        ProductResponse expectedResponse = new ProductResponse(1L, "Test Product", "Description", "1.5 KG", "0.0 KG", "0.0 KG", "url", null, new BigDecimal("10.0"), 100L, null, "12345", null, null, null);

        given(quantityParserService.parseAndValidate(validRequestDto.stockQuantity())).willReturn(parsedQuantity);
        given(masterTree.getById(validRequestDto.stockUnitId())).willReturn(mockUnitNode);
        given(masterTree.isChildOf(mockUnitNode, "UNIT")).willReturn(true);
        given(unitConversionService.convertToSmallestUnit(parsedQuantity, "KG")).willReturn(convertedStock);

        given(productOutletRepositoryPort.save(any(ProductDomain.class))).willAnswer(invocation -> {
            ProductDomain arg = invocation.getArgument(0);
            return ProductDomain.fromPersistence(1L, arg.getName(), arg.getDescription(), arg.getStock(), arg.getUnitMeasureId(), arg.getImageUrl(), arg.getPrice(), arg.getOutletId(), arg.getStatusId(), arg.getCreatedAt(), arg.getUpdatedAt(), arg.getVersion());
        });
        given(productOutletMapper.toResponse(any(ProductDomain.class), any())).willReturn(expectedResponse);

        ProductResponse result = createProductUseCase.execute(validRequestDto);

        assertNotNull(result);
        assertEquals(expectedResponse, result);

        ArgumentCaptor<ProductDomain> captor = ArgumentCaptor.forClass(ProductDomain.class);
        verify(productOutletRepositoryPort).save(captor.capture());
        ProductDomain savedDomain = captor.getValue();

        assertEquals(convertedStock, savedDomain.getStock());
        assertEquals(validRequestDto.stockUnitId(), savedDomain.getUnitMeasureId());
        assertEquals(1L, savedDomain.getStatusId());
    }

    @Test
    @DisplayName("Deberia lanzar excepcion si la cantidad tiene un formato invalido")
    void shouldThrowExceptionIfQuantityFormatIsInvalid() {
        given(quantityParserService.parseAndValidate(validRequestDto.stockQuantity()))
                .willThrow(new DomainValidationException("Invalid number format for quantity"));

        DomainValidationException exception = assertThrows(DomainValidationException.class, () -> {
            createProductUseCase.execute(validRequestDto);
        });

        assertEquals("Invalid number format for quantity", exception.getMessage());
        verifyNoInteractions(unitConversionService, productOutletRepositoryPort, productOutletMapper);
    }

    @Test
    @DisplayName("Deberia lanzar excepcion si el ID de la unidad no existe")
    void shouldThrowExceptionIfUnitIdDoesNotExist() {
        given(quantityParserService.parseAndValidate(validRequestDto.stockQuantity())).willReturn(new BigDecimal("1.5"));
        given(masterTree.getById(validRequestDto.stockUnitId())).willReturn(null);

        DomainValidationException exception = assertThrows(DomainValidationException.class, () -> {
            createProductUseCase.execute(validRequestDto);
        });

        assertEquals("The provided stock unit ID does not exist.", exception.getMessage());
        verifyNoInteractions(unitConversionService, productOutletRepositoryPort, productOutletMapper);
    }

    @Test
    @DisplayName("Deberia lanzar excepcion si el ID no es hijo de UNIT")
    void shouldThrowExceptionIfIdIsNotAUnit() {
        given(quantityParserService.parseAndValidate(validRequestDto.stockQuantity())).willReturn(new BigDecimal("1.5"));
        given(masterTree.getById(validRequestDto.stockUnitId())).willReturn(mockUnitNode);
        given(masterTree.isChildOf(mockUnitNode, "UNIT")).willReturn(false);

        DomainValidationException exception = assertThrows(DomainValidationException.class, () -> {
            createProductUseCase.execute(validRequestDto);
        });

        assertEquals("The provided ID is not a valid unit of measurement.", exception.getMessage());
        verifyNoInteractions(unitConversionService, productOutletRepositoryPort, productOutletMapper);
    }
}
