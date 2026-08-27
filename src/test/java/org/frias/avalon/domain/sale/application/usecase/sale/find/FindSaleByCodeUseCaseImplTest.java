package org.frias.avalon.domain.sale.application.usecase.sale.find;

import org.frias.avalon.core.exeptions.ResourceNotFoundException;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("Unit Tests for FindSaleByCodeUseCaseImpl in POS Sales Domain")
class FindSaleByCodeUseCaseImplTest {

    private SaleRepositoryPort saleRepositoryPort;
    private PersonRepositoryPort personRepositoryPort;
    private ProductOutletRepositoryPort productOutletRepositoryPort;
    private MasterTreeProvider masterTreeProvider;

    private FindSaleByCodeUseCaseImpl findSaleByCodeUseCase;

    private final UUID defaultSaleUuid = UUID.randomUUID();
    private final Long outletId = 1L;
    private final Long clientId = 20L;
    private final Long productId = 10L;

    @BeforeEach
    void setUp() {
        saleRepositoryPort = mock(SaleRepositoryPort.class);
        personRepositoryPort = mock(PersonRepositoryPort.class);
        productOutletRepositoryPort = mock(ProductOutletRepositoryPort.class);
        masterTreeProvider = mock(MasterTreeProvider.class);

        findSaleByCodeUseCase = new FindSaleByCodeUseCaseImpl(
                saleRepositoryPort,
                personRepositoryPort,
                productOutletRepositoryPort,
                masterTreeProvider
        );
    }

    @Nested
    @DisplayName("Exception Handling Tests")
    class ExceptionTests {

        @Test
        @DisplayName("Should throw ResourceNotFoundException when sale with code is not found")
        void shouldThrowExceptionWhenSaleNotFound() {
            when(saleRepositoryPort.findByCode(defaultSaleUuid)).thenReturn(Optional.empty());

            ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                    () -> findSaleByCodeUseCase.execute(defaultSaleUuid));
            assertEquals("Venta con código '" + defaultSaleUuid + "' no encontrada.", ex.getMessage());
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when client associated with sale is not found")
        void shouldThrowExceptionWhenClientNotFound() {
            SaleDomain sale = SaleDomain.fromPersistence(
                    100L, defaultSaleUuid, new BigDecimal("10000"), new BigDecimal("10000"), BigDecimal.ZERO,
                    1L, outletId, clientId, 1L, 15L, LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), List.of()
            );
            when(saleRepositoryPort.findByCode(defaultSaleUuid)).thenReturn(Optional.of(sale));
            when(personRepositoryPort.findById(clientId)).thenReturn(Optional.empty());

            ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                    () -> findSaleByCodeUseCase.execute(defaultSaleUuid));
            assertEquals("Cliente asociado a la venta no encontrado", ex.getMessage());
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when product in sale item is not found in catalog")
        void shouldThrowExceptionWhenProductNotFound() {
            SaleItemDomain item = new SaleItemDomain(1L, productId, 2, "2 UN", new BigDecimal("5000"), new BigDecimal("10000"), 1L);
            SaleDomain sale = SaleDomain.fromPersistence(
                    100L, defaultSaleUuid, new BigDecimal("10000"), new BigDecimal("10000"), BigDecimal.ZERO,
                    1L, outletId, clientId, 1L, 15L, LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), List.of(item)
            );
            when(saleRepositoryPort.findByCode(defaultSaleUuid)).thenReturn(Optional.of(sale));

            PersonDomain client = PersonDomain.createFromEntity(
                    clientId, "12345678", "PEDRO", "GOMEZ", "CALLE 3",
                    1L, 1L, 5551111L, "pedro@email.com", 1L, LocalDateTime.now(), LocalDateTime.now()
            );
            when(personRepositoryPort.findById(clientId)).thenReturn(Optional.of(client));

            MasterTree masterTree = mock(MasterTree.class);
            when(masterTreeProvider.getTree()).thenReturn(masterTree);

            when(productOutletRepositoryPort.findById(productId)).thenReturn(Optional.empty());

            ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                    () -> findSaleByCodeUseCase.execute(defaultSaleUuid));
            assertEquals("Producto con ID " + productId + " no encontrado", ex.getMessage());
        }
    }

    @Nested
    @DisplayName("Successful Retrieval & Mapping Tests")
    class MappingSuccessTests {

        @Test
        @DisplayName("Should execute successfully and return mapped SaleResponse with master data and items")
        void shouldExecuteSuccessfullyWithFullDetails() {
            SaleItemDomain item = new SaleItemDomain(1L, productId, 2, "2 UN", new BigDecimal("5000.00"), new BigDecimal("10000.00"), 1L);
            SaleDomain sale = SaleDomain.fromPersistence(
                    100L, defaultSaleUuid, new BigDecimal("10000.00"), new BigDecimal("10000.00"), BigDecimal.ZERO,
                    2L, outletId, clientId, 3L, 15L, LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), List.of(item)
            );
            when(saleRepositoryPort.findByCode(defaultSaleUuid)).thenReturn(Optional.of(sale));

            PersonDomain client = PersonDomain.createFromEntity(
                    clientId, "12345678", "PEDRO", "GOMEZ", "CALLE 3",
                    1L, 1L, 5551111L, "pedro@email.com", 1L, LocalDateTime.now(), LocalDateTime.now()
            );
            when(personRepositoryPort.findById(clientId)).thenReturn(Optional.of(client));

            MasterTree masterTree = mock(MasterTree.class);
            when(masterTreeProvider.getTree()).thenReturn(masterTree);
            when(masterTree.getById(2L)).thenReturn(new MasterRoot(2L, "TAR", "Tarjeta", 0L, 1L));
            when(masterTree.getById(3L)).thenReturn(new MasterRoot(3L, "CMP", "Completada", 0L, 1L));

            ProductDomain product = ProductDomain.fromPersistence(
                    productId, "Aceite 1L", "Aceite vegetal", 20, 1L, "", new BigDecimal("5000.00"), outletId, 1L, LocalDateTime.now(), LocalDateTime.now()
            );
            when(productOutletRepositoryPort.findById(productId)).thenReturn(Optional.of(product));

            SaleResponse response = findSaleByCodeUseCase.execute(defaultSaleUuid);

            assertNotNull(response);
            assertEquals(100L, response.id());
            assertEquals(defaultSaleUuid, response.saleCode());
            assertEquals(new BigDecimal("10000.00"), response.totalAmount());
            assertNotNull(response.paymentMethod());
            assertEquals("TAR", response.paymentMethod().shortName());
            assertEquals("Tarjeta", response.paymentMethod().fullName());
            assertNotNull(response.status());
            assertEquals("CMP", response.status().shortName());
            assertEquals("Completada", response.status().fullName());
            assertEquals("PEDRO GOMEZ", response.clientFullName());
            assertEquals("12345678", response.clientNumberid());
            assertEquals(1, response.items().size());
            assertEquals("Aceite 1L", response.items().get(0).productName());
        }

        @Test
        @DisplayName("Should execute successfully returning null master DTOs when nodes are missing in MasterTree")
        void shouldExecuteSuccessfullyWithNullMasterNodes() {
            SaleDomain sale = SaleDomain.fromPersistence(
                    101L, defaultSaleUuid, new BigDecimal("8000.00"), new BigDecimal("10000.00"), new BigDecimal("2000.00"),
                    99L, outletId, clientId, 98L, 15L, LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), List.of()
            );
            when(saleRepositoryPort.findByCode(defaultSaleUuid)).thenReturn(Optional.of(sale));

            PersonDomain client = PersonDomain.createFromEntity(
                    clientId, "12345678", "LUIS", "RODRIGUEZ", "CALLE 4",
                    1L, 1L, 5552222L, "luis@email.com", 1L, LocalDateTime.now(), LocalDateTime.now()
            );
            when(personRepositoryPort.findById(clientId)).thenReturn(Optional.of(client));

            MasterTree masterTree = mock(MasterTree.class);
            when(masterTreeProvider.getTree()).thenReturn(masterTree);
            when(masterTree.getById(99L)).thenReturn(null);
            when(masterTree.getById(98L)).thenReturn(null);

            SaleResponse response = findSaleByCodeUseCase.execute(defaultSaleUuid);

            assertNotNull(response);
            assertEquals(101L, response.id());
            assertNull(response.paymentMethod());
            assertNull(response.status());
            assertEquals("LUIS RODRIGUEZ", response.clientFullName());
            assertTrue(response.items().isEmpty());
        }
    }
}
