package org.frias.avalon.domain.sale.application.usecase.sale.find;

import org.frias.avalon.core.exeptions.BusinessException;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("Unit Tests for FindAllSalesUseCaseImpl in POS Sales Domain")
class FindAllSalesUseCaseImplTest {

    private SaleRepositoryPort saleRepositoryPort;
    private PersonRepositoryPort personRepositoryPort;
    private ProductOutletRepositoryPort productOutletRepositoryPort;
    private MasterTreeProvider masterTreeProvider;
    private CurrentUserProviderPort currentUserProvider;

    private FindAllSalesUseCaseImpl findAllSalesUseCase;

    private final Long outletId = 1L;
    private final Long clientId = 20L;
    private final Long productId = 10L;
    private final Pageable pageable = PageRequest.of(0, 10);

    @BeforeEach
    void setUp() {
        saleRepositoryPort = mock(SaleRepositoryPort.class);
        personRepositoryPort = mock(PersonRepositoryPort.class);
        productOutletRepositoryPort = mock(ProductOutletRepositoryPort.class);
        masterTreeProvider = mock(MasterTreeProvider.class);
        currentUserProvider = mock(CurrentUserProviderPort.class);

        findAllSalesUseCase = new FindAllSalesUseCaseImpl(
                saleRepositoryPort,
                personRepositoryPort,
                productOutletRepositoryPort,
                masterTreeProvider,
                currentUserProvider
        );
    }

    @Nested
    @DisplayName("Tenant Security and Validation Tests")
    class TenantSecurityTests {

        @Test
        @DisplayName("Should throw BusinessException when non-admin has no tenant outlet ID in context")
        void shouldThrowExceptionWhenNonAdminHasNoTenantOutlet() {
            when(currentUserProvider.hasRole("ROLE_ADMIN")).thenReturn(false);
            when(currentUserProvider.hasRole("ROLE_ADMINTI")).thenReturn(false);
            when(currentUserProvider.getCurrentOutletId()).thenReturn(null);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> findAllSalesUseCase.execute(outletId, pageable));
            assertEquals("No se detectó una tienda asociada en el contexto del empleado actual.", ex.getMessage());
        }

        @Test
        @DisplayName("Should throw BusinessException when non-admin attempts to access another outlet's sales")
        void shouldThrowExceptionWhenNonAdminAccessesOtherOutlet() {
            when(currentUserProvider.hasRole("ROLE_ADMIN")).thenReturn(false);
            when(currentUserProvider.hasRole("ROLE_ADMINTI")).thenReturn(false);
            when(currentUserProvider.getCurrentOutletId()).thenReturn(1L);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> findAllSalesUseCase.execute(2L, pageable));
            assertEquals("Acceso denegado: No tienes permisos para listar ventas de otra tienda.", ex.getMessage());
        }

        @Test
        @DisplayName("Should throw BusinessException when finalOutletId is null (Admin passing null outletId)")
        void shouldThrowExceptionWhenFinalOutletIdIsNull() {
            when(currentUserProvider.hasRole("ROLE_ADMIN")).thenReturn(true);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> findAllSalesUseCase.execute(null, pageable));
            assertEquals("Se requiere especificar el ID de la tienda para listar las ventas.", ex.getMessage());
        }

        @Test
        @DisplayName("Should allow execution when non-admin accesses own outlet with matching or null requested outletId")
        void shouldAllowExecutionForNonAdminWithOwnOutlet() {
            when(currentUserProvider.hasRole("ROLE_ADMIN")).thenReturn(false);
            when(currentUserProvider.hasRole("ROLE_ADMINTI")).thenReturn(false);
            when(currentUserProvider.getCurrentOutletId()).thenReturn(outletId);

            MasterTree masterTree = mock(MasterTree.class);
            when(masterTreeProvider.getTree()).thenReturn(masterTree);
            when(saleRepositoryPort.findByOutletId(outletId, pageable)).thenReturn(Page.empty(pageable));

            Page<SaleResponse> result = findAllSalesUseCase.execute(null, pageable);

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Should allow execution when user has ROLE_ADMINTI even for different outlet")
        void shouldAllowExecutionForAdminTi() {
            when(currentUserProvider.hasRole("ROLE_ADMIN")).thenReturn(false);
            when(currentUserProvider.hasRole("ROLE_ADMINTI")).thenReturn(true);

            MasterTree masterTree = mock(MasterTree.class);
            when(masterTreeProvider.getTree()).thenReturn(masterTree);
            when(saleRepositoryPort.findByOutletId(5L, pageable)).thenReturn(Page.empty(pageable));

            Page<SaleResponse> result = findAllSalesUseCase.execute(5L, pageable);

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("Sale Mapping and Domain Validation Tests")
    class SaleMappingTests {

        @Test
        @DisplayName("Should throw ResourceNotFoundException when sale client is not found in repository")
        void shouldThrowExceptionWhenClientNotFound() {
            when(currentUserProvider.hasRole("ROLE_ADMIN")).thenReturn(true);

            SaleDomain sale = SaleDomain.fromPersistence(
                    100L, UUID.randomUUID(), new BigDecimal("10000"), new BigDecimal("10000"), BigDecimal.ZERO,
                    1L, outletId, clientId, 1L, 15L, LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), List.of()
            );
            Page<SaleDomain> salePage = new PageImpl<>(List.of(sale), pageable, 1);

            MasterTree masterTree = mock(MasterTree.class);
            when(masterTreeProvider.getTree()).thenReturn(masterTree);
            when(saleRepositoryPort.findByOutletId(outletId, pageable)).thenReturn(salePage);
            when(personRepositoryPort.findById(clientId)).thenReturn(Optional.empty());

            ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                    () -> findAllSalesUseCase.execute(outletId, pageable));
            assertEquals("Cliente asociado a la venta no encontrado", ex.getMessage());
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when sale item product is not found in catalog")
        void shouldThrowExceptionWhenProductNotFound() {
            when(currentUserProvider.hasRole("ROLE_ADMIN")).thenReturn(true);

            SaleItemDomain item = new SaleItemDomain(1L, productId, 2, "2 UN", new BigDecimal("5000"), new BigDecimal("10000"), 1L);
            SaleDomain sale = SaleDomain.fromPersistence(
                    100L, UUID.randomUUID(), new BigDecimal("10000"), new BigDecimal("10000"), BigDecimal.ZERO,
                    1L, outletId, clientId, 1L, 15L, LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), List.of(item)
            );
            Page<SaleDomain> salePage = new PageImpl<>(List.of(sale), pageable, 1);

            MasterTree masterTree = mock(MasterTree.class);
            when(masterTreeProvider.getTree()).thenReturn(masterTree);
            when(saleRepositoryPort.findByOutletId(outletId, pageable)).thenReturn(salePage);

            PersonDomain client = PersonDomain.createFromEntity(
                    clientId, "12345678", "MARIA", "LOPEZ", "AV 2",
                    1L, 1L, 5554321L, "maria@email.com", 1L, LocalDateTime.now(), LocalDateTime.now()
            );
            when(personRepositoryPort.findById(clientId)).thenReturn(Optional.of(client));
            when(productOutletRepositoryPort.findById(productId)).thenReturn(Optional.empty());

            ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                    () -> findAllSalesUseCase.execute(outletId, pageable));
            assertEquals("Producto con ID " + productId + " no encontrado", ex.getMessage());
        }

        @Test
        @DisplayName("Should map sales successfully with full master data and items")
        void shouldMapSalesSuccessfullyWithMasterDataAndItems() {
            when(currentUserProvider.hasRole("ROLE_ADMIN")).thenReturn(true);

            UUID saleUuid = UUID.randomUUID();
            SaleItemDomain item = new SaleItemDomain(1L, productId, 2, "2 UN", new BigDecimal("5000.00"), new BigDecimal("10000.00"), 1L);
            SaleDomain sale = SaleDomain.fromPersistence(
                    100L, saleUuid, new BigDecimal("10000.00"), new BigDecimal("10000.00"), BigDecimal.ZERO,
                    2L, outletId, clientId, 3L, 15L, LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), List.of(item)
            );
            Page<SaleDomain> salePage = new PageImpl<>(List.of(sale), pageable, 1);

            MasterTree masterTree = mock(MasterTree.class);
            when(masterTreeProvider.getTree()).thenReturn(masterTree);
            when(masterTree.getById(2L)).thenReturn(new MasterRoot(2L, "EFE", "Efectivo", 0L, 1L));
            when(masterTree.getById(3L)).thenReturn(new MasterRoot(3L, "CMP", "Completada", 0L, 1L));

            when(saleRepositoryPort.findByOutletId(outletId, pageable)).thenReturn(salePage);

            PersonDomain client = PersonDomain.createFromEntity(
                    clientId, "12345678", "CARLOS", "SANCHEZ", "CALLE 5",
                    1L, 1L, 5559999L, "carlos@email.com", 1L, LocalDateTime.now(), LocalDateTime.now()
            );
            when(personRepositoryPort.findById(clientId)).thenReturn(Optional.of(client));

            ProductDomain product = ProductDomain.fromPersistence(
                    productId, "Arroz 1kg", "Arroz", 50, 1L, "", new BigDecimal("5000.00"), outletId, 1L, LocalDateTime.now(), LocalDateTime.now()
            );
            when(productOutletRepositoryPort.findById(productId)).thenReturn(Optional.of(product));

            Page<SaleResponse> result = findAllSalesUseCase.execute(outletId, pageable);

            assertNotNull(result);
            assertEquals(1, result.getTotalElements());
            SaleResponse response = result.getContent().get(0);

            assertEquals(100L, response.id());
            assertEquals(saleUuid, response.saleCode());
            assertEquals(new BigDecimal("10000.00"), response.totalAmount());
            assertNotNull(response.paymentMethod());
            assertEquals("EFE", response.paymentMethod().shortName());
            assertEquals("Efectivo", response.paymentMethod().fullName());
            assertNotNull(response.status());
            assertEquals("CMP", response.status().shortName());
            assertEquals("Completada", response.status().fullName());
            assertEquals("CARLOS SANCHEZ", response.clientFullName());
            assertEquals("12345678", response.clientNumberid());
            assertEquals(1, response.items().size());
            assertEquals("Arroz 1kg", response.items().get(0).productName());
        }

        @Test
        @DisplayName("Should map sales successfully with null master data nodes (null payment and status DTOs)")
        void shouldMapSalesSuccessfullyWithNullMasterNodes() {
            when(currentUserProvider.hasRole("ROLE_ADMIN")).thenReturn(true);

            UUID saleUuid = UUID.randomUUID();
            SaleDomain sale = SaleDomain.fromPersistence(
                    101L, saleUuid, new BigDecimal("5000.00"), new BigDecimal("5000.00"), BigDecimal.ZERO,
                    99L, outletId, clientId, 99L, 15L, LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), List.of()
            );
            Page<SaleDomain> salePage = new PageImpl<>(List.of(sale), pageable, 1);

            MasterTree masterTree = mock(MasterTree.class);
            when(masterTreeProvider.getTree()).thenReturn(masterTree);
            when(masterTree.getById(99L)).thenReturn(null); // Null master node

            when(saleRepositoryPort.findByOutletId(outletId, pageable)).thenReturn(salePage);

            PersonDomain client = PersonDomain.createFromEntity(
                    clientId, "87654321", "ANA", "GOMEZ", "CALLE 10",
                    1L, 1L, 5550000L, "ana@email.com", 1L, LocalDateTime.now(), LocalDateTime.now()
            );
            when(personRepositoryPort.findById(clientId)).thenReturn(Optional.of(client));

            Page<SaleResponse> result = findAllSalesUseCase.execute(outletId, pageable);

            assertNotNull(result);
            assertEquals(1, result.getTotalElements());
            SaleResponse response = result.getContent().get(0);

            assertNull(response.paymentMethod());
            assertNull(response.status());
            assertEquals("ANA GOMEZ", response.clientFullName());
        }
    }
}
