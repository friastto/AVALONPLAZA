package org.frias.avalon.domain.product.application.usecase.create;

import org.frias.avalon.core.exeptions.DomainValidationException;
import org.frias.avalon.domain.masterdata.domain.model.MasterRoot;
import org.frias.avalon.domain.masterdata.domain.model.MasterTree;
import org.frias.avalon.domain.masterdata.domain.repository.MasterDataRepositoryPort;
import org.frias.avalon.domain.masterdata.domain.service.MasterTreeProvider;
import org.frias.avalon.domain.product.application.dto.request.ProductNewDataRequest;
import org.frias.avalon.domain.product.application.dto.response.ProductResponse;
import org.frias.avalon.domain.product.application.port.ProductOutletRepositoryPort;
import org.frias.avalon.domain.product.application.service.QuantityParserService;
import org.frias.avalon.domain.product.domain.ProductDomain;
import org.frias.avalon.domain.product.domain.service.UnitConversionService;
import org.frias.avalon.domain.product.infraestructure.mapper.ProductOutletMapper;
import org.frias.avalon.domain.product.domain.repository.BarcodeRepositoryPort;
import org.frias.avalon.core.permissions.CurrentUserProviderPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

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
    private MasterDataRepositoryPort masterDataRepositoryPort;
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

    @InjectMocks
    private CreateProductOutletUseCaseImpl createProductUseCase;

    private ProductNewDataRequest validRequestDto;
    private MasterRoot mockUnitNode;

    @BeforeEach
    void setUp() {
        lenient().when(currentUserProvider.hasRole(anyString())).thenReturn(true); // Permitir admin en tests
        lenient().when(barcodeRepositoryPort.findByCode(any())).thenReturn(java.util.Optional.empty());

        validRequestDto = new ProductNewDataRequest(
                "",
                "Test Product",
                "Description",
                "1.3",
                50L, // stockUnitId
                "url",
                new BigDecimal("10.0"),
                100L // outletId
        );

        mockUnitNode = new MasterRoot(50L, "KG", "Kilogramos", 10L, 1L);
    }

    @Test
    @DisplayName("Debería crear un producto exitosamente tras parsear y validar la unidad")
    void shouldCreateProductSuccessfully() {
        // Arrange
        Long activeStatusId = 1L;
        Integer convertedStock = 1500;
        BigDecimal parsedQuantity = new BigDecimal("1.5");
        ProductResponse expectedResponse = new ProductResponse(1L, "Test Product", "Description", "1.5 KG", "0.0 KG", "0.0 KG", "url", null, new BigDecimal("10.0"), 100L, null, "12345",null, null,null);


        given(masterDataRepositoryPort.getIdByCode("ACT")).willReturn(activeStatusId);
        
        // Simular el nuevo servicio de parsing
        given(quantityParserService.parseAndValidate(validRequestDto.stockQuantity())).willReturn(parsedQuantity);
        
        given(masterTreeProvider.getTree()).willReturn(masterTree);
        given(masterTree.getById(validRequestDto.stockUnitId())).willReturn(mockUnitNode);
        given(masterTree.isChildOf(mockUnitNode, "UNIT")).willReturn(true);
        
        // La fábrica de conversión ahora recibe el BigDecimal parseado
        given(unitConversionService.convertToSmallestUnit(parsedQuantity, "KG")).willReturn(convertedStock);
        
        given(productOutletRepositoryPort.save(any(ProductDomain.class))).willAnswer(invocation -> {
            ProductDomain arg = invocation.getArgument(0);
            return ProductDomain.fromPersistence(1L, arg.getName(), arg.getDescription(), arg.getStock(), arg.getUnitMeasureId(), arg.getImageUrl(), arg.getPrice(), arg.getOutletId(), arg.getStatusId(), arg.getCreatedAt(), arg.getUpdatedAt(), arg.getVersion());
        });
        given(productOutletMapper.toResponse(any(ProductDomain.class), any())).willReturn(expectedResponse);

        // Act
        ProductResponse result = createProductUseCase.execute(validRequestDto);

        // Assert
        assertNotNull(result);
        assertEquals(expectedResponse, result);

        ArgumentCaptor<ProductDomain> captor = ArgumentCaptor.forClass(ProductDomain.class);
        verify(productOutletRepositoryPort).save(captor.capture());
        ProductDomain savedDomain = captor.getValue();
        
        assertEquals(convertedStock, savedDomain.getStock());
        assertEquals(validRequestDto.stockUnitId(), savedDomain.getUnitMeasureId());
    }

    @Test
    @DisplayName("Debería lanzar excepción si la cantidad tiene un formato inválido")
    void shouldThrowExceptionIfQuantityFormatIsInvalid() {
        // Arrange
        given(masterDataRepositoryPort.getIdByCode("ACT")).willReturn(1L);
        // Simulamos que el parser falla (ej. si el usuario mandó "0.0.5")
        given(quantityParserService.parseAndValidate(validRequestDto.stockQuantity()))
            .willThrow(new DomainValidationException("Invalid number format for quantity"));

        // Act & Assert
        DomainValidationException exception = assertThrows(DomainValidationException.class, () -> {
            createProductUseCase.execute(validRequestDto);
        });

        assertEquals("Invalid number format for quantity", exception.getMessage());
        
        // Verificamos que no se intentó hacer nada más después de fallar el parsing
        verifyNoInteractions(masterTreeProvider, unitConversionService, productOutletRepositoryPort, productOutletMapper);
    }

    @Test
    @DisplayName("Debería lanzar excepción si el ID de la unidad no existe")
    void shouldThrowExceptionIfUnitIdDoesNotExist() {
        // Arrange
        given(masterDataRepositoryPort.getIdByCode("ACT")).willReturn(1L);
        given(quantityParserService.parseAndValidate(validRequestDto.stockQuantity())).willReturn(new BigDecimal("1.5"));
        given(masterTreeProvider.getTree()).willReturn(masterTree);
        given(masterTree.getById(validRequestDto.stockUnitId())).willReturn(null);

        // Act & Assert
        DomainValidationException exception = assertThrows(DomainValidationException.class, () -> {
            createProductUseCase.execute(validRequestDto);
        });

        assertEquals("The provided stock unit ID does not exist.", exception.getMessage());
        verifyNoInteractions(unitConversionService, productOutletRepositoryPort, productOutletMapper);
    }

    @Test
    @DisplayName("Debería lanzar excepción si el ID no es hijo de UNIT")
    void shouldThrowExceptionIfIdIsNotAUnit() {
        // Arrange
        given(masterDataRepositoryPort.getIdByCode("ACT")).willReturn(1L);
        given(quantityParserService.parseAndValidate(validRequestDto.stockQuantity())).willReturn(new BigDecimal("1.5"));
        given(masterTreeProvider.getTree()).willReturn(masterTree);
        given(masterTree.getById(validRequestDto.stockUnitId())).willReturn(mockUnitNode);
        given(masterTree.isChildOf(mockUnitNode, "UNIT")).willReturn(false);

        // Act & Assert
        DomainValidationException exception = assertThrows(DomainValidationException.class, () -> {
            createProductUseCase.execute(validRequestDto);
        });

        assertEquals("The provided ID is not a valid unit of measurement.", exception.getMessage());
        verifyNoInteractions(unitConversionService, productOutletRepositoryPort, productOutletMapper);
    }
}
