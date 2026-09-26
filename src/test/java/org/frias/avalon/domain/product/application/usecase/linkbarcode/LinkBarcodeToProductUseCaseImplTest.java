package org.frias.avalon.domain.product.application.usecase.linkbarcode;

import org.frias.avalon.core.exeptions.ResourceNotFoundException;
import org.frias.avalon.core.tenant.TenantContext;
import org.frias.avalon.domain.outlet.infraestructure.repository.JpaOutletRepository;
import org.frias.avalon.domain.product.application.dto.request.LinkBarcodeRequest;
import org.frias.avalon.domain.product.application.port.ProductOutletRepositoryPort;
import org.frias.avalon.domain.product.domain.BarcodeDomain;
import org.frias.avalon.domain.product.domain.ProductDomain;
import org.frias.avalon.domain.product.domain.repository.BarcodeRepositoryPort;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas Unitarias para LinkBarcodeToProductUseCaseImpl")
class LinkBarcodeToProductUseCaseImplTest {

    @Mock
    private ProductOutletRepositoryPort productOutletRepositoryPort;
    @Mock
    private BarcodeRepositoryPort barcodeRepositoryPort;
    @Mock
    private JpaOutletRepository jpaOutletRepository;
    @Mock
    private PlatformTransactionManager transactionManager;

    private LinkBarcodeToProductUseCaseImpl linkBarcodeToProductUseCase;

    private LinkBarcodeRequest validRequest;
    private ProductDomain existingProduct;

    @BeforeEach
    void setUp() {
        TransactionStatus transactionStatus = mock(TransactionStatus.class);
        lenient().when(transactionManager.getTransaction(any())).thenReturn(transactionStatus);
        lenient().when(jpaOutletRepository.findAll()).thenReturn(Collections.emptyList());

        linkBarcodeToProductUseCase = new LinkBarcodeToProductUseCaseImpl(
                productOutletRepositoryPort,
                barcodeRepositoryPort,
                jpaOutletRepository,
                transactionManager
        );

        validRequest = new LinkBarcodeRequest(1L, "123456789012", "Descripcion del codigo");
        existingProduct = ProductDomain.create(
                "Producto Test", "Descripcion", 100, 1L, "url",
                java.math.BigDecimal.valueOf(10.0), 1L, 1L
        );
        TenantContext.setTenantOutletId(1L);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("Deberia vincular un codigo de barras a un producto existente exitosamente")
    void shouldLinkBarcodeToExistingProductSuccessfully() {
        when(productOutletRepositoryPort.findById(validRequest.productId())).thenReturn(Optional.of(existingProduct));
        when(barcodeRepositoryPort.save(any(BarcodeDomain.class))).thenAnswer(invocation -> invocation.getArgument(0));

        linkBarcodeToProductUseCase.execute(validRequest);

        verify(productOutletRepositoryPort, times(1)).findById(validRequest.productId());
        ArgumentCaptor<BarcodeDomain> barcodeCaptor = ArgumentCaptor.forClass(BarcodeDomain.class);
        verify(barcodeRepositoryPort, times(1)).save(barcodeCaptor.capture());

        BarcodeDomain savedBarcode = barcodeCaptor.getValue();
        assertEquals(validRequest.barcode(), savedBarcode.getBarcode());
        assertEquals(validRequest.productId(), savedBarcode.getProductOutletId());
    }

    @Test
    @DisplayName("Deberia lanzar excepcion si el producto al que se vincula no existe")
    void shouldThrowExceptionWhenProductDoesNotExist() {
        when(productOutletRepositoryPort.findById(validRequest.productId())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            linkBarcodeToProductUseCase.execute(validRequest);
        });

        verify(barcodeRepositoryPort, never()).save(any());
    }
}
