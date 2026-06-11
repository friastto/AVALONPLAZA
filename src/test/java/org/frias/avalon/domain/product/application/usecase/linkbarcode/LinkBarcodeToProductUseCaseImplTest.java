package org.frias.avalon.domain.product.application.usecase.linkbarcode;

import org.frias.avalon.domain.product.application.dto.request.LinkBarcodeRequest;
import org.frias.avalon.domain.product.application.port.ProductOutletRepositoryPort;
import org.frias.avalon.domain.product.domain.BarcodeDomain;
import org.frias.avalon.domain.product.domain.ProductDomain;
import org.frias.avalon.domain.product.domain.repository.BarcodeRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas Unitarias para LinkBarcodeToProductUseCaseImpl")
class LinkBarcodeToProductUseCaseImplTest {

    @Mock
    private ProductOutletRepositoryPort productOutletRepositoryPort;
    @Mock
    private BarcodeRepositoryPort barcodeRepositoryPort;

    @InjectMocks
    private LinkBarcodeToProductUseCaseImpl linkBarcodeToProductUseCase;

    private LinkBarcodeRequest validRequest;
    private ProductDomain existingProduct;

    @BeforeEach
    void setUp() {
        validRequest = new LinkBarcodeRequest(1L, "123456789012", "Descripción del código");
        existingProduct = ProductDomain.create(
                "Producto Test", "Descripción", 100, 1L, "url",
                java.math.BigDecimal.valueOf(10.0), 1L, 1L
        );
    }

    @Test
    @DisplayName("Debería vincular un código de barras a un producto existente exitosamente")
    void shouldLinkBarcodeToExistingProductSuccessfully() {
        // Arrange
        when(productOutletRepositoryPort.findById(validRequest.productId())).thenReturn(Optional.of(existingProduct));
        when(barcodeRepositoryPort.save(any(BarcodeDomain.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        linkBarcodeToProductUseCase.execute(validRequest);

        // Assert
        verify(productOutletRepositoryPort, times(1)).findById(validRequest.productId());
        ArgumentCaptor<BarcodeDomain> barcodeCaptor = ArgumentCaptor.forClass(BarcodeDomain.class);
        verify(barcodeRepositoryPort, times(1)).save(barcodeCaptor.capture());

        BarcodeDomain savedBarcode = barcodeCaptor.getValue();
        assertNotNull(savedBarcode);
        assertEquals(validRequest.barcode(), savedBarcode.getBarcode());
        assertEquals(validRequest.productId(), savedBarcode.getProductOutletId());
        assertEquals(validRequest.description(), savedBarcode.getDescription());
    }

    @Test
    @DisplayName("No debería vincular si el código de barras es nulo o vacío")
    void shouldNotLinkIfBarcodeIsNullOrEmpty() {
        // Arrange
        LinkBarcodeRequest nullBarcodeRequest = new LinkBarcodeRequest(1L, null, "desc");
        LinkBarcodeRequest emptyBarcodeRequest = new LinkBarcodeRequest(1L, "", "desc");
        LinkBarcodeRequest blankBarcodeRequest = new LinkBarcodeRequest(1L, "   ", "desc");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> linkBarcodeToProductUseCase.execute(nullBarcodeRequest));
        assertThrows(IllegalArgumentException.class, () -> linkBarcodeToProductUseCase.execute(emptyBarcodeRequest));
        assertThrows(IllegalArgumentException.class, () -> linkBarcodeToProductUseCase.execute(blankBarcodeRequest));

        verify(productOutletRepositoryPort, never()).findById(anyLong());
        verify(barcodeRepositoryPort, never()).save(any(BarcodeDomain.class));
    }

    @Test
    @DisplayName("Debería lanzar IllegalArgumentException si el producto no existe")
    void shouldThrowExceptionIfProductDoesNotExist() {
        // Arrange
        when(productOutletRepositoryPort.findById(validRequest.productId())).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> linkBarcodeToProductUseCase.execute(validRequest));

        assertEquals("Producto no encontrado con ID: " + validRequest.productId(), exception.getMessage());
        verify(productOutletRepositoryPort, times(1)).findById(validRequest.productId());
        verify(barcodeRepositoryPort, never()).save(any(BarcodeDomain.class));
    }

    @Test
    @DisplayName("Debería lanzar IllegalArgumentException si el productId es nulo en la request")
    void shouldThrowExceptionIfProductIdIsNullInRequest() {
        // Arrange
        LinkBarcodeRequest requestWithNullProductId = new LinkBarcodeRequest(null, "123", "desc");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> linkBarcodeToProductUseCase.execute(requestWithNullProductId));
        verify(productOutletRepositoryPort, never()).findById(anyLong());
        verify(barcodeRepositoryPort, never()).save(any(BarcodeDomain.class));
    }
}
