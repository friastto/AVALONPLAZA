package org.frias.avalon.domain.sale.application.usecase.sale.find;

import org.frias.avalon.core.exeptions.ResourceNotFoundException;
import org.frias.avalon.core.permissions.CurrentUserProviderPort;
import org.frias.avalon.domain.masterdata.domain.model.MasterRoot;
import org.frias.avalon.domain.masterdata.domain.model.MasterTree;
import org.frias.avalon.domain.masterdata.domain.service.MasterTreeProvider;
import org.frias.avalon.domain.person.domain.model.PersonDomain;
import org.frias.avalon.domain.person.domain.port.PersonRepositoryPort;
import org.frias.avalon.domain.product.application.port.ProductOutletRepositoryPort;
import org.frias.avalon.domain.product.domain.ProductDomain;
import org.frias.avalon.domain.sale.application.dto.response.SaleResponse;
import org.frias.avalon.domain.sale.application.port.SaleRepositoryPort;
import org.frias.avalon.domain.sale.domain.SaleDomain;
import org.frias.avalon.domain.sale.domain.SaleItemDomain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@DisplayName("Unit Tests for SearchSalesUseCaseImpl in POS Sales Search Domain")
class SearchSalesUseCaseImplTest {

    private SaleRepositoryPort saleRepositoryPort;
    private PersonRepositoryPort personRepositoryPort;
    private ProductOutletRepositoryPort productOutletRepositoryPort;
    private MasterTreeProvider masterTreeProvider;
    private CurrentUserProviderPort currentUserProvider;

    private SearchSalesUseCaseImpl searchSalesUseCase;

    @BeforeEach
    void setUp() {
        saleRepositoryPort = mock(SaleRepositoryPort.class);
        personRepositoryPort = mock(PersonRepositoryPort.class);
        productOutletRepositoryPort = mock(ProductOutletRepositoryPort.class);
        masterTreeProvider = mock(MasterTreeProvider.class);
        currentUserProvider = mock(CurrentUserProviderPort.class);

        searchSalesUseCase = new SearchSalesUseCaseImpl(
                saleRepositoryPort,
                personRepositoryPort,
                productOutletRepositoryPort,
                masterTreeProvider,
                currentUserProvider
        );
    }

    @Test
    @DisplayName("Should search sales flexibly by text query")
    void shouldSearchSalesByFlexibleQuery() {
        when(currentUserProvider.hasRole("ROLE_ADMIN")).thenReturn(true);

        UUID saleUuid = UUID.randomUUID();
        SaleItemDomain item = new SaleItemDomain(1L, 10L, 2, "2 UN", new BigDecimal("5000"), new BigDecimal("10000"), 1L);
        SaleDomain sale = SaleDomain.fromPersistence(
                100L, saleUuid, new BigDecimal("10000"), new BigDecimal("10000"), BigDecimal.ZERO,
                1L, 1L, 20L, 1L, 15L, LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), List.of(item)
        );

        when(saleRepositoryPort.flexibleSearch(eq(1L), eq("juan"), any(Pageable.class))).thenReturn(List.of(sale));

        PersonDomain client = PersonDomain.createFromEntity(
                20L, "12345678", "JUAN", "PEREZ", "CALLE 1",
                1L, 1L, 5551234L, "juan@email.com", 1L, LocalDateTime.now(), LocalDateTime.now()
        );
        when(personRepositoryPort.findById(20L)).thenReturn(Optional.of(client));

        MasterTree masterTree = mock(MasterTree.class);
        when(masterTreeProvider.getTree()).thenReturn(masterTree);
        when(masterTree.getById(1L)).thenReturn(new MasterRoot(1L, "EFE", "Efectivo", 0L, 1L));

        ProductDomain product = ProductDomain.fromPersistence(
                10L, "Jabon 100g", "Jabon", 10, 1L, "", new BigDecimal("5000.00"), 1L, 1L, LocalDateTime.now(), LocalDateTime.now()
        );
        when(productOutletRepositoryPort.findById(10L)).thenReturn(Optional.of(product));

        List<SaleResponse> results = searchSalesUseCase.search(1L, "juan");

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("JUAN PEREZ", results.get(0).clientFullName());
    }

    @Test
    @DisplayName("Should find sale by UUID string using findByFlexibleCode")
    void shouldFindSaleByUuidString() {
        UUID saleUuid = UUID.randomUUID();
        when(currentUserProvider.hasRole("ROLE_ADMIN")).thenReturn(true);

        SaleDomain sale = SaleDomain.fromPersistence(
                100L, saleUuid, new BigDecimal("10000"), new BigDecimal("10000"), BigDecimal.ZERO,
                1L, 1L, 20L, 1L, 15L, LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), List.of()
        );

        when(saleRepositoryPort.findByCode(saleUuid)).thenReturn(Optional.of(sale));

        MasterTree masterTree = mock(MasterTree.class);
        when(masterTreeProvider.getTree()).thenReturn(masterTree);

        SaleResponse response = searchSalesUseCase.findByFlexibleCode(saleUuid.toString(), 1L);

        assertNotNull(response);
        assertEquals(saleUuid, response.saleCode());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when flexible code search finds nothing")
    void shouldThrowExceptionWhenCodeNotFound() {
        when(currentUserProvider.hasRole("ROLE_ADMIN")).thenReturn(true);
        when(saleRepositoryPort.flexibleSearch(eq(1L), eq("INVALID_CODE"), any(Pageable.class))).thenReturn(List.of());

        assertThrows(ResourceNotFoundException.class, () -> searchSalesUseCase.findByFlexibleCode("INVALID_CODE", 1L));
    }
}
