package org.frias.avalon.domain.product.application.usecase.find;

import org.frias.avalon.core.exeptions.BusinessException;
import org.frias.avalon.core.permissions.CurrentUserProviderPort;
import org.frias.avalon.domain.outlet.domain.model.OutletDomain;
import org.frias.avalon.domain.outlet.domain.port.OutletRepositoryPort;
import org.frias.avalon.domain.product.application.dto.response.ProductResponse;
import org.frias.avalon.domain.product.application.port.ProductOutletRepositoryPort;
import org.frias.avalon.domain.product.domain.ProductDomain;
import org.frias.avalon.domain.product.infraestructure.mapper.ProductOutletMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("Unit Tests for FindProductCatalogByOutletUseCaseImpl Tenant Isolation")
class FindProductCatalogByOutletUseCaseImplTest {

    private ProductOutletRepositoryPort productOutletRepositoryPort;
    private ProductOutletMapper productOutletMapper;
    private CurrentUserProviderPort currentUserProvider;
    private OutletRepositoryPort outletPort;
    private PlatformTransactionManager transactionManager;

    private FindProductCatalogByOutletUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        productOutletRepositoryPort = mock(ProductOutletRepositoryPort.class);
        productOutletMapper = mock(ProductOutletMapper.class);
        currentUserProvider = mock(CurrentUserProviderPort.class);
        outletPort = mock(OutletRepositoryPort.class);
        transactionManager = mock(PlatformTransactionManager.class);

        TransactionStatus transactionStatus = mock(TransactionStatus.class);
        when(transactionManager.getTransaction(any())).thenReturn(transactionStatus);

        useCase = new FindProductCatalogByOutletUseCaseImpl(
                productOutletRepositoryPort,
                productOutletMapper,
                currentUserProvider,
                outletPort,
                transactionManager
        );
    }

    @Test
    @DisplayName("Should throw BusinessException when employee tries to access another store catalog")
    void shouldThrowExceptionWhenEmployeeAccessesOtherStoreCatalog() {
        when(currentUserProvider.hasRole("ROLE_CLIENT")).thenReturn(false);
        when(currentUserProvider.hasRole("ROLE_CONSUMER")).thenReturn(false);
        when(currentUserProvider.hasRole("ROLE_ADMIN")).thenReturn(false);
        when(currentUserProvider.hasRole("ROLE_ADMINTI")).thenReturn(false);
        when(currentUserProvider.getCurrentOutletId()).thenReturn(1L);

        Pageable pageable = PageRequest.of(0, 10);
        assertThrows(BusinessException.class, () -> useCase.execute(2L, null, null, pageable));
    }

    @Test
    @DisplayName("Should return catalog successfully for consumer user")
    void shouldReturnCatalogForConsumerUser() {
        when(currentUserProvider.hasRole("ROLE_CLIENT")).thenReturn(true);
        when(currentUserProvider.hasRole("ROLE_CONSUMER")).thenReturn(false);

        OutletDomain outlet = new OutletDomain(
                1L, "OUT-1", "Tienda 1", "Calle 1", "5551234", "900123456", 1L, null,
                new BigDecimal("500000"), true, new BigDecimal("3000"), 2L, LocalDateTime.now(), LocalDateTime.now()
        );
        when(outletPort.findById(1L)).thenReturn(Optional.of(outlet));

        ProductDomain product = ProductDomain.fromPersistence(
                10L, "Leche Entera", "1L", 20, 1L, "", new BigDecimal("4200.00"), 1L, 1L, LocalDateTime.now(), LocalDateTime.now()
        );

        Page<ProductDomain> productPage = new PageImpl<>(List.of(product));
        when(productOutletRepositoryPort.findAll(null, 1L, null, PageRequest.of(0, 10))).thenReturn(productPage);

        ProductResponse productResponse = new ProductResponse(
                10L, "Leche Entera", "1L", "20 UND", "0 UND", "0 UND", "", "", new BigDecimal("4200.00"), 1L, null, null, 1L, LocalDateTime.now(), LocalDateTime.now()
        );
        when(productOutletMapper.toResponse(product)).thenReturn(productResponse);

        Pageable pageable = PageRequest.of(0, 10);
        Page<ProductResponse> result = useCase.execute(1L, null, null, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Leche Entera", result.getContent().get(0).name());
    }
}
