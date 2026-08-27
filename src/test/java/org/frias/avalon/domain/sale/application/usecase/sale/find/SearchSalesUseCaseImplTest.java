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
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
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

    private final Long outletId = 1L;
    private final Long clientId = 20L;
    private final Long productId = 10L;

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

    @Nested
    @DisplayName("Tenant Resolution (getEffectiveOutletId) Tests")
    class EffectiveOutletIdTests {

        @Test
        @DisplayName("Should use requested outletId when user has ROLE_ADMIN")
        void shouldReturnRequestedOutletIdWhenUserIsAdmin() {
            when(currentUserProvider.hasRole("ROLE_ADMIN")).thenReturn(true);
            when(saleRepositoryPort.flexibleSearch(eq(5L), eq("query"), any(Pageable.class)))
                    .thenReturn(List.of());

            List<SaleResponse> results = searchSalesUseCase.search(5L, "query");

            assertNotNull(results);
            verify(saleRepositoryPort).flexibleSearch(eq(5L), eq("query"), eq(PageRequest.of(0, 20)));
        }

        @Test
        @DisplayName("Should use requested outletId when user has ROLE_ADMINTI")
        void shouldReturnRequestedOutletIdWhenUserIsAdminTi() {
            when(currentUserProvider.hasRole("ROLE_ADMIN")).thenReturn(false);
            when(currentUserProvider.hasRole("ROLE_ADMINTI")).thenReturn(true);
            when(saleRepositoryPort.findRecentSales(5L)).thenReturn(List.of());

            List<SaleResponse> results = searchSalesUseCase.getRecentSales(5L);

            assertNotNull(results);
            verify(saleRepositoryPort).findRecentSales(5L);
        }

        @Test
        @DisplayName("Should use tenant outletId for non-admin user when tenant outletId is present")
        void shouldReturnTenantOutletIdForNonAdmin() {
            when(currentUserProvider.hasRole("ROLE_ADMIN")).thenReturn(false);
            when(currentUserProvider.hasRole("ROLE_ADMINTI")).thenReturn(false);
            when(currentUserProvider.getCurrentOutletId()).thenReturn(99L);
            when(saleRepositoryPort.findRecentSales(99L)).thenReturn(List.of());

            List<SaleResponse> results = searchSalesUseCase.getRecentSales(1L);

            assertNotNull(results);
            verify(saleRepositoryPort).findRecentSales(99L);
        }

        @Test
        @DisplayName("Should fallback to passed outletId for non-admin user when tenant outletId is null")
        void shouldFallbackToPassedOutletIdWhenTenantOutletIdIsNull() {
            when(currentUserProvider.hasRole("ROLE_ADMIN")).thenReturn(false);
            when(currentUserProvider.hasRole("ROLE_ADMINTI")).thenReturn(false);
            when(currentUserProvider.getCurrentOutletId()).thenReturn(null);
            when(saleRepositoryPort.findRecentSales(1L)).thenReturn(List.of());

            List<SaleResponse> results = searchSalesUseCase.getRecentSales(1L);

            assertNotNull(results);
            verify(saleRepositoryPort).findRecentSales(1L);
        }
    }

    @Nested
    @DisplayName("search() and getRecentSales() Tests")
    class SearchAndRecentSalesTests {

        @Test
        @DisplayName("Should search sales flexibly by text query with limit of 20")
        void shouldSearchSalesByFlexibleQuery() {
            when(currentUserProvider.hasRole("ROLE_ADMIN")).thenReturn(true);

            UUID saleUuid = UUID.randomUUID();
            SaleItemDomain item = new SaleItemDomain(1L, productId, 2, "2 UN", new BigDecimal("5000"), new BigDecimal("10000"), 1L);
            SaleDomain sale = SaleDomain.fromPersistence(
                    100L, saleUuid, new BigDecimal("10000"), new BigDecimal("10000"), BigDecimal.ZERO,
                    1L, outletId, clientId, 1L, 15L, LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), List.of(item)
            );

            when(saleRepositoryPort.flexibleSearch(eq(outletId), eq("juan"), eq(PageRequest.of(0, 20))))
                    .thenReturn(List.of(sale));

            PersonDomain client = PersonDomain.createFromEntity(
                    clientId, "12345678", "JUAN", "PEREZ", "CALLE 1",
                    1L, 1L, 5551234L, "juan@email.com", 1L, LocalDateTime.now(), LocalDateTime.now()
            );
            when(personRepositoryPort.findById(clientId)).thenReturn(Optional.of(client));

            MasterTree masterTree = mock(MasterTree.class);
            when(masterTreeProvider.getTree()).thenReturn(masterTree);
            when(masterTree.getById(1L)).thenReturn(new MasterRoot(1L, "EFE", "Efectivo", 0L, 1L));

            ProductDomain product = ProductDomain.fromPersistence(
                    productId, "Jabon 100g", "Jabon", 10, 1L, "", new BigDecimal("5000.00"), outletId, 1L, LocalDateTime.now(), LocalDateTime.now()
            );
            when(productOutletRepositoryPort.findById(productId)).thenReturn(Optional.of(product));

            List<SaleResponse> results = searchSalesUseCase.search(outletId, "juan");

            assertNotNull(results);
            assertEquals(1, results.size());
            assertEquals("JUAN PEREZ", results.get(0).clientFullName());
        }

        @Test
        @DisplayName("Should get recent sales successfully")
        void shouldGetRecentSalesSuccessfully() {
            when(currentUserProvider.hasRole("ROLE_ADMIN")).thenReturn(true);

            UUID saleUuid = UUID.randomUUID();
            SaleDomain sale = SaleDomain.fromPersistence(
                    101L, saleUuid, new BigDecimal("15000"), new BigDecimal("20000"), new BigDecimal("5000"),
                    1L, outletId, clientId, 1L, 15L, LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), List.of()
            );

            when(saleRepositoryPort.findRecentSales(outletId)).thenReturn(List.of(sale));

            MasterTree masterTree = mock(MasterTree.class);
            when(masterTreeProvider.getTree()).thenReturn(masterTree);

            List<SaleResponse> results = searchSalesUseCase.getRecentSales(outletId);

            assertNotNull(results);
            assertEquals(1, results.size());
            assertEquals(101L, results.get(0).id());
        }
    }

    @Nested
    @DisplayName("findByFlexibleCode() Tests")
    class FindByFlexibleCodeTests {

        @Test
        @DisplayName("Should find sale by UUID string when UUID exists")
        void shouldFindSaleByUuidString() {
            UUID saleUuid = UUID.randomUUID();
            when(currentUserProvider.hasRole("ROLE_ADMIN")).thenReturn(true);

            SaleDomain sale = SaleDomain.fromPersistence(
                    100L, saleUuid, new BigDecimal("10000"), new BigDecimal("10000"), BigDecimal.ZERO,
                    1L, outletId, clientId, 1L, 15L, LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), List.of()
            );

            when(saleRepositoryPort.findByCode(saleUuid)).thenReturn(Optional.of(sale));

            MasterTree masterTree = mock(MasterTree.class);
            when(masterTreeProvider.getTree()).thenReturn(masterTree);

            SaleResponse response = searchSalesUseCase.findByFlexibleCode(saleUuid.toString(), outletId);

            assertNotNull(response);
            assertEquals(saleUuid, response.saleCode());
        }

        @Test
        @DisplayName("Should fallback when UUID string format is valid but sale is not found by UUID")
        void shouldFallbackWhenUuidStringNotFound() {
            UUID saleUuid = UUID.randomUUID();
            when(currentUserProvider.hasRole("ROLE_ADMIN")).thenReturn(true);

            when(saleRepositoryPort.findByCode(saleUuid)).thenReturn(Optional.empty());

            SaleDomain flexibleSale = SaleDomain.fromPersistence(
                    200L, UUID.randomUUID(), new BigDecimal("5000"), new BigDecimal("5000"), BigDecimal.ZERO,
                    1L, outletId, clientId, 1L, 15L, LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), List.of()
            );
            when(saleRepositoryPort.flexibleSearch(eq(outletId), eq(saleUuid.toString()), eq(PageRequest.of(0, 1))))
                    .thenReturn(List.of(flexibleSale));

            MasterTree masterTree = mock(MasterTree.class);
            when(masterTreeProvider.getTree()).thenReturn(masterTree);

            SaleResponse response = searchSalesUseCase.findByFlexibleCode(saleUuid.toString(), outletId);

            assertNotNull(response);
            assertEquals(200L, response.id());
        }

        @Test
        @DisplayName("Should find sale by numeric ID string when numeric ID exists")
        void shouldFindSaleByNumericIdString() {
            when(currentUserProvider.hasRole("ROLE_ADMIN")).thenReturn(true);

            SaleDomain sale = SaleDomain.fromPersistence(
                    105L, UUID.randomUUID(), new BigDecimal("12000"), new BigDecimal("15000"), new BigDecimal("3000"),
                    1L, outletId, clientId, 1L, 15L, LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), List.of()
            );

            when(saleRepositoryPort.findById(105L)).thenReturn(Optional.of(sale));

            MasterTree masterTree = mock(MasterTree.class);
            when(masterTreeProvider.getTree()).thenReturn(masterTree);

            SaleResponse response = searchSalesUseCase.findByFlexibleCode(" 105 ", outletId);

            assertNotNull(response);
            assertEquals(105L, response.id());
            verify(saleRepositoryPort).findById(105L);
        }

        @Test
        @DisplayName("Should fallback when numeric ID string is valid number but sale is not found by ID")
        void shouldFallbackWhenNumericIdNotFound() {
            when(currentUserProvider.hasRole("ROLE_ADMIN")).thenReturn(true);

            when(saleRepositoryPort.findById(999L)).thenReturn(Optional.empty());

            SaleDomain flexibleSale = SaleDomain.fromPersistence(
                    300L, UUID.randomUUID(), new BigDecimal("4000"), new BigDecimal("4000"), BigDecimal.ZERO,
                    1L, outletId, clientId, 1L, 15L, LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), List.of()
            );
            when(saleRepositoryPort.flexibleSearch(eq(outletId), eq("999"), eq(PageRequest.of(0, 1))))
                    .thenReturn(List.of(flexibleSale));

            MasterTree masterTree = mock(MasterTree.class);
            when(masterTreeProvider.getTree()).thenReturn(masterTree);

            SaleResponse response = searchSalesUseCase.findByFlexibleCode("999", outletId);

            assertNotNull(response);
            assertEquals(300L, response.id());
        }

        @Test
        @DisplayName("Should find sale by flexible search query (e.g. shortCode or client name) when non-numeric and non-UUID")
        void shouldFindSaleByFlexibleSearchQuery() {
            when(currentUserProvider.hasRole("ROLE_ADMIN")).thenReturn(true);

            SaleDomain flexibleSale = SaleDomain.fromPersistence(
                    400L, UUID.randomUUID(), new BigDecimal("7000"), new BigDecimal("7000"), BigDecimal.ZERO,
                    1L, outletId, clientId, 1L, 15L, LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), List.of()
            );
            when(saleRepositoryPort.flexibleSearch(eq(outletId), eq("SHORTCODE123"), eq(PageRequest.of(0, 1))))
                    .thenReturn(List.of(flexibleSale));

            MasterTree masterTree = mock(MasterTree.class);
            when(masterTreeProvider.getTree()).thenReturn(masterTree);

            SaleResponse response = searchSalesUseCase.findByFlexibleCode("SHORTCODE123", outletId);

            assertNotNull(response);
            assertEquals(400L, response.id());
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when flexible code search finds nothing across all steps")
        void shouldThrowExceptionWhenCodeNotFound() {
            when(currentUserProvider.hasRole("ROLE_ADMIN")).thenReturn(true);
            when(saleRepositoryPort.flexibleSearch(eq(outletId), eq("INVALID_CODE"), eq(PageRequest.of(0, 1))))
                    .thenReturn(List.of());

            ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                    () -> searchSalesUseCase.findByFlexibleCode("INVALID_CODE", outletId));
            assertTrue(ex.getMessage().contains("No se encontró ninguna venta con el criterio o código: INVALID_CODE"));
        }

        @Test
        @DisplayName("Should handle null input string in findByFlexibleCode gracefully")
        void shouldHandleNullInputStringInFindByFlexibleCode() {
            when(currentUserProvider.hasRole("ROLE_ADMIN")).thenReturn(true);
            when(saleRepositoryPort.flexibleSearch(eq(outletId), eq(""), eq(PageRequest.of(0, 1))))
                    .thenReturn(List.of());

            ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                    () -> searchSalesUseCase.findByFlexibleCode(null, outletId));
            assertTrue(ex.getMessage().contains("No se encontró ninguna venta con el criterio o código:"));
        }
    }

    @Nested
    @DisplayName("mapToResponse Helper Mapping Edge Cases")
    class MapToResponseEdgeCasesTests {

        @Test
        @DisplayName("Should map response with fallback 'Cliente General' and empty clientNumberid when client is missing")
        void shouldMapResponseWithFallbackClientInfoWhenMissing() {
            when(currentUserProvider.hasRole("ROLE_ADMIN")).thenReturn(true);

            SaleDomain sale = SaleDomain.fromPersistence(
                    100L, UUID.randomUUID(), new BigDecimal("10000"), new BigDecimal("10000"), BigDecimal.ZERO,
                    1L, outletId, clientId, 1L, 15L, LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), List.of()
            );
            when(saleRepositoryPort.findRecentSales(outletId)).thenReturn(List.of(sale));
            when(personRepositoryPort.findById(clientId)).thenReturn(Optional.empty());

            MasterTree masterTree = mock(MasterTree.class);
            when(masterTreeProvider.getTree()).thenReturn(masterTree);

            List<SaleResponse> results = searchSalesUseCase.getRecentSales(outletId);

            assertNotNull(results);
            assertEquals(1, results.size());
            assertEquals("Cliente General", results.get(0).clientFullName());
            assertEquals("", results.get(0).clientNumberid());
        }

        @Test
        @DisplayName("Should map response with fallback product name 'Producto #ID' when product is missing in catalog")
        void shouldMapResponseWithFallbackProductNameWhenMissing() {
            when(currentUserProvider.hasRole("ROLE_ADMIN")).thenReturn(true);

            SaleItemDomain item = new SaleItemDomain(1L, 999L, 1, "1 UN", new BigDecimal("5000"), new BigDecimal("5000"), 1L);
            SaleDomain sale = SaleDomain.fromPersistence(
                    100L, UUID.randomUUID(), new BigDecimal("5000"), new BigDecimal("5000"), BigDecimal.ZERO,
                    1L, outletId, clientId, 1L, 15L, LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), List.of(item)
            );
            when(saleRepositoryPort.findRecentSales(outletId)).thenReturn(List.of(sale));

            MasterTree masterTree = mock(MasterTree.class);
            when(masterTreeProvider.getTree()).thenReturn(masterTree);

            when(productOutletRepositoryPort.findById(999L)).thenReturn(Optional.empty());

            List<SaleResponse> results = searchSalesUseCase.getRecentSales(outletId);

            assertNotNull(results);
            assertEquals(1, results.size());
            assertEquals(1, results.get(0).items().size());
            assertEquals("Producto #999", results.get(0).items().get(0).productName());
        }

        @Test
        @DisplayName("Should map response with empty items list when sale.getItems() is null")
        void shouldMapResponseWithEmptyItemsListWhenSaleItemsIsNull() {
            when(currentUserProvider.hasRole("ROLE_ADMIN")).thenReturn(true);

            SaleDomain sale = SaleDomain.fromPersistence(
                    100L, UUID.randomUUID(), new BigDecimal("5000"), new BigDecimal("5000"), BigDecimal.ZERO,
                    1L, outletId, clientId, 1L, 15L, LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), null
            );
            when(saleRepositoryPort.findRecentSales(outletId)).thenReturn(List.of(sale));

            MasterTree masterTree = mock(MasterTree.class);
            when(masterTreeProvider.getTree()).thenReturn(masterTree);

            List<SaleResponse> results = searchSalesUseCase.getRecentSales(outletId);

            assertNotNull(results);
            assertEquals(1, results.size());
            assertNotNull(results.get(0).items());
            assertTrue(results.get(0).items().isEmpty());
        }
    }
}
