package org.frias.avalon.domain.sale.application.usecase.sale.returns;

import org.frias.avalon.domain.person.domain.model.PersonDomain;
import org.frias.avalon.domain.person.domain.port.PersonRepositoryPort;
import org.frias.avalon.domain.sale.application.dto.response.ReturnResponse;
import org.frias.avalon.domain.sale.application.port.ReturnRepositoryPort;
import org.frias.avalon.domain.sale.application.port.SaleRepositoryPort;
import org.frias.avalon.domain.sale.domain.ReturnDomain;
import org.frias.avalon.domain.sale.domain.ReturnItemDomain;
import org.frias.avalon.domain.sale.domain.SaleDomain;
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

@DisplayName("Unit Tests for FindReturnsUseCaseImpl in POS Return Domain")
class FindReturnsUseCaseImplTest {

    private ReturnRepositoryPort returnRepositoryPort;
    private SaleRepositoryPort saleRepositoryPort;
    private PersonRepositoryPort personRepositoryPort;

    private FindReturnsUseCaseImpl findReturnsUseCase;

    private final UUID defaultReturnUuid = UUID.randomUUID();
    private final UUID defaultSaleUuid = UUID.randomUUID();
    private final Long outletId = 1L;
    private final Long clientId = 20L;
    private final Long employeeId = 15L;
    private final Long originalSaleId = 100L;
    private final Long productId = 10L;

    @BeforeEach
    void setUp() {
        returnRepositoryPort = mock(ReturnRepositoryPort.class);
        saleRepositoryPort = mock(SaleRepositoryPort.class);
        personRepositoryPort = mock(PersonRepositoryPort.class);

        findReturnsUseCase = new FindReturnsUseCaseImpl(
                returnRepositoryPort,
                saleRepositoryPort,
                personRepositoryPort
        );
    }

    @Nested
    @DisplayName("findByCode Tests")
    class FindByCodeTests {

        @Test
        @DisplayName("Should return empty optional when return code does not exist")
        void shouldReturnEmptyOptionalWhenNotFound() {
            when(returnRepositoryPort.findByCode(defaultReturnUuid)).thenReturn(Optional.empty());

            Optional<ReturnResponse> result = findReturnsUseCase.findByCode(defaultReturnUuid);

            assertTrue(result.isEmpty());
            verify(returnRepositoryPort, times(1)).findByCode(defaultReturnUuid);
            verifyNoInteractions(saleRepositoryPort, personRepositoryPort);
        }

        @Test
        @DisplayName("Should return ReturnResponse with full details when return, original sale, and client exist")
        void shouldReturnReturnResponseWithFullDetails() {
            ReturnItemDomain returnItem = new ReturnItemDomain(
                    1L, productId, 2, "2 UN", new BigDecimal("3500.00"), new BigDecimal("7000.00"), 1L
            );
            ReturnDomain returnDomain = ReturnDomain.fromPersistence(
                    1L, defaultReturnUuid, originalSaleId, new BigDecimal("7000.00"),
                    "DEFECTO", "Empaque roto", "REEMBOLSO", 50L, employeeId, outletId, clientId,
                    LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), List.of(returnItem)
            );
            when(returnRepositoryPort.findByCode(defaultReturnUuid)).thenReturn(Optional.of(returnDomain));

            SaleDomain originalSale = SaleDomain.fromPersistence(
                    originalSaleId, defaultSaleUuid, new BigDecimal("17500.00"), new BigDecimal("20000.00"), new BigDecimal("2500.00"),
                    5L, outletId, clientId, 1L, employeeId, LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), List.of()
            );
            when(saleRepositoryPort.findById(originalSaleId)).thenReturn(Optional.of(originalSale));

            PersonDomain client = PersonDomain.createFromEntity(
                    clientId, "12345678", "JUAN", "PEREZ", "CALLE 1",
                    1L, 1L, 5551234L, "juan@email.com", 1L, LocalDateTime.now(), LocalDateTime.now()
            );
            when(personRepositoryPort.findById(clientId)).thenReturn(Optional.of(client));

            Optional<ReturnResponse> result = findReturnsUseCase.findByCode(defaultReturnUuid);

            assertTrue(result.isPresent());
            ReturnResponse response = result.get();

            assertEquals(1L, response.id());
            assertEquals(defaultReturnUuid, response.returnCode());
            assertEquals(defaultSaleUuid, response.originalSaleCode());
            assertEquals(originalSaleId, response.originalSaleId());
            assertEquals(new BigDecimal("7000.00"), response.totalRefundAmount());
            assertEquals("DEFECTO", response.reason());
            assertEquals("Empaque roto", response.notes());
            assertEquals("REEMBOLSO", response.resolutionType());
            assertEquals("DEV", response.status());
            assertEquals("JUAN PEREZ", response.clientFullName());
            assertEquals("12345678", response.clientNumberid());
            assertEquals(outletId, response.outletId());
            assertEquals(employeeId, response.employeeId());
            assertNotNull(response.returnDate());
            assertEquals(1, response.items().size());
            assertEquals(productId, response.items().get(0).productId());
            assertEquals("Producto #" + productId, response.items().get(0).productName());
            assertEquals("2 UN", response.items().get(0).displayQuantity());
            assertEquals(new BigDecimal("3500.00"), response.items().get(0).unitPrice());
            assertEquals(new BigDecimal("7000.00"), response.items().get(0).subtotal());
        }

        @Test
        @DisplayName("Should return ReturnResponse with null sale code and fallback client info when sale and client are missing")
        void shouldReturnResponseWithFallbackClientAndNullSaleCodeWhenMissing() {
            ReturnItemDomain returnItem = new ReturnItemDomain(
                    2L, productId, 1, "1 UN", new BigDecimal("5000.00"), new BigDecimal("5000.00"), 1L
            );
            ReturnDomain returnDomain = ReturnDomain.fromPersistence(
                    2L, defaultReturnUuid, originalSaleId, new BigDecimal("5000.00"),
                    "OTRO", "Motivo vario", "CAMBIO", 50L, employeeId, outletId, clientId,
                    LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), List.of(returnItem)
            );
            when(returnRepositoryPort.findByCode(defaultReturnUuid)).thenReturn(Optional.of(returnDomain));
            when(saleRepositoryPort.findById(originalSaleId)).thenReturn(Optional.empty());
            when(personRepositoryPort.findById(clientId)).thenReturn(Optional.empty());

            Optional<ReturnResponse> result = findReturnsUseCase.findByCode(defaultReturnUuid);

            assertTrue(result.isPresent());
            ReturnResponse response = result.get();

            assertNull(response.originalSaleCode());
            assertEquals("Desconocido", response.clientFullName());
            assertEquals("", response.clientNumberid());
        }
    }

    @Nested
    @DisplayName("findByOutlet Tests")
    class FindByOutletTests {

        @Test
        @DisplayName("Should return Page of ReturnResponse when returns exist for outlet")
        void shouldReturnPageOfReturnResponseWhenReturnsExist() {
            Pageable pageable = PageRequest.of(0, 10);
            ReturnItemDomain returnItem = new ReturnItemDomain(
                    1L, productId, 1, "1 UN", new BigDecimal("1000.00"), new BigDecimal("1000.00"), 1L
            );
            ReturnDomain returnDomain = ReturnDomain.fromPersistence(
                    10L, defaultReturnUuid, originalSaleId, new BigDecimal("1000.00"),
                    "INCORRECTO", "Notas", "NOTA_CREDITO", 50L, employeeId, outletId, clientId,
                    LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), List.of(returnItem)
            );
            Page<ReturnDomain> domainPage = new PageImpl<>(List.of(returnDomain), pageable, 1);

            when(returnRepositoryPort.findByOutletId(outletId, pageable)).thenReturn(domainPage);
            when(saleRepositoryPort.findById(originalSaleId)).thenReturn(Optional.empty());
            when(personRepositoryPort.findById(clientId)).thenReturn(Optional.empty());

            Page<ReturnResponse> result = findReturnsUseCase.findByOutlet(outletId, pageable);

            assertNotNull(result);
            assertEquals(1, result.getTotalElements());
            assertEquals(10L, result.getContent().get(0).id());
            assertEquals("INCORRECTO", result.getContent().get(0).reason());
        }

        @Test
        @DisplayName("Should return empty Page when no returns exist for outlet")
        void shouldReturnEmptyPageWhenNoReturnsFound() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<ReturnDomain> emptyPage = Page.empty(pageable);

            when(returnRepositoryPort.findByOutletId(outletId, pageable)).thenReturn(emptyPage);

            Page<ReturnResponse> result = findReturnsUseCase.findByOutlet(outletId, pageable);

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }
}
