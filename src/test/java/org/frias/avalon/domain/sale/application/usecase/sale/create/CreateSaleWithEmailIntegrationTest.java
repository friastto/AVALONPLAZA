package org.frias.avalon.domain.sale.application.usecase.sale.create;

import org.frias.avalon.core.permissions.CurrentUserProviderPort;
import org.frias.avalon.core.permissions.UserContext;
import org.frias.avalon.domain.credit.application.port.CreditRepositoryPort;
import org.frias.avalon.domain.masterdata.domain.repository.MasterDataRepositoryPort;
import org.frias.avalon.domain.notification.application.port.EmailSenderPort;
import org.frias.avalon.domain.person.domain.model.PersonDomain;
import org.frias.avalon.domain.person.domain.port.PersonRepositoryPort;
import org.frias.avalon.domain.product.application.port.ProductOutletRepositoryPort;
import org.frias.avalon.domain.product.domain.ProductDomain;
import org.frias.avalon.domain.sale.application.dto.request.CreateSaleRequest;
import org.frias.avalon.domain.sale.application.dto.request.SaleItemRequest;
import org.frias.avalon.domain.sale.application.dto.response.SaleResponse;
import org.frias.avalon.domain.sale.application.port.SaleRepositoryPort;
import org.frias.avalon.domain.sale.domain.SaleDomain;
import org.frias.avalon.domain.user.domain.model.UserAvalonDomain;
import org.frias.avalon.domain.user.domain.port.UserAvalonRepositoryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
@EnableAutoConfiguration(exclude = {MailSenderAutoConfiguration.class})
public class CreateSaleWithEmailIntegrationTest {

    @Autowired
    private CreateSaleUseCase createSaleUseCase;

    @MockitoBean
    private SaleRepositoryPort saleRepositoryPort;

    @MockitoBean
    private ProductOutletRepositoryPort productOutletRepositoryPort;

    @MockitoBean
    private PersonRepositoryPort personRepositoryPort;

    @MockitoBean
    private UserAvalonRepositoryPort userAvalonRepositoryPort;

    @MockitoBean
    private MasterDataRepositoryPort masterDataRepositoryPort;

    @MockitoBean
    private CurrentUserProviderPort currentUserProvider;

    @MockitoBean
    private CreditRepositoryPort creditRepositoryPort;

    @MockitoBean
    private EmailSenderPort emailSenderPort;

    @Test
    @DisplayName("Should create sale and asynchronously publish email event triggering EmailSenderPort")
    public void testCreateSaleAndSendEmail() {
        System.out.println("--- STARTING CREATE SALE & EMAIL INTEGRATION TEST ---");

        Long outletId = 4L;
        String clientDoc = "999888777";
        String clientEmail = "cliente.test@avalon.com";
        String sellerUsername = "test_seller_user";

        // 1. Mock Security Context
        UserContext mockContext = new UserContext(sellerUsername, List.of("ROLE_ADMINTI"), outletId);
        when(currentUserProvider.getCurrentUserContext()).thenReturn(mockContext);
        when(currentUserProvider.getCurrentOutletId()).thenReturn(outletId);
        when(currentUserProvider.hasRole("ROLE_ADMIN")).thenReturn(true);
        when(currentUserProvider.hasRole("ROLE_ADMINTI")).thenReturn(true);

        // 2. Mock User and Person
        UserAvalonDomain employeeUser = UserAvalonDomain.fromPersistenceBasic(10L, 100L, sellerUsername, 2L);
        when(userAvalonRepositoryPort.findByUserName(sellerUsername)).thenReturn(Optional.of(employeeUser));

        PersonDomain clientPerson = PersonDomain.createFromEntity(
                200L, clientDoc, "Cliente", "Prueba Email", "Calle 123",
                1L, 1L, 987654321L, clientEmail, 2L, LocalDateTime.now(), LocalDateTime.now()
        );
        when(personRepositoryPort.findByNumberid(clientDoc)).thenReturn(Optional.of(clientPerson));

        // 3. Mock MasterData
        when(masterDataRepositoryPort.getIdByCode("ACT")).thenReturn(2L);

        // 4. Mock Product (UND is unit ID 14 in seed)
        ProductDomain testProduct = ProductDomain.fromPersistence(
                50L, "Producto Arroz Test", "Bolsa 1kg", 100, 14L, "img.jpg",
                new BigDecimal("1500.00"), outletId, 2L, LocalDateTime.now(), LocalDateTime.now()
        );
        when(productOutletRepositoryPort.findById(50L)).thenReturn(Optional.of(testProduct));
        when(productOutletRepositoryPort.save(any(ProductDomain.class))).thenAnswer(inv -> inv.getArgument(0));

        // 5. Mock Sale Repository
        when(saleRepositoryPort.save(any(SaleDomain.class))).thenAnswer(inv -> {
            SaleDomain sale = inv.getArgument(0);
            return SaleDomain.fromPersistence(
                    1001L, UUID.randomUUID(), sale.getTotalAmount(), sale.getAmountReceived(), sale.getChangeGiven(),
                    sale.getPaymentMethodId(), sale.getStatusId(), sale.getClientId(), sale.getOutletId(),
                    sale.getEmployeeId(), LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), List.of()
            );
        });

        // 6. Execute Use Case
        SaleItemRequest itemRequest = new SaleItemRequest(50L, "2");
        CreateSaleRequest saleRequest = new CreateSaleRequest(
                clientDoc,
                outletId,
                11L, // Efectivo (EFE)
                new BigDecimal("5000.00"),
                List.of(itemRequest),
                true // sendEmail = true
        );

        SaleResponse saleResponse = createSaleUseCase.execute(saleRequest);

        // 7. Verify Sale Response
        assertNotNull(saleResponse);
        assertEquals(outletId, saleResponse.outletId());
        assertEquals("Cliente Prueba Email", saleResponse.clientFullName());
        assertEquals(new BigDecimal("3000.00"), saleResponse.totalAmount());
        assertEquals(new BigDecimal("2000.00"), saleResponse.changeGiven());

        // 8. Verify Async Email Sending via Mockito timeout
        verify(emailSenderPort, Mockito.timeout(5000)).sendEmailWithAttachment(
                eq(clientEmail),
                any(String.class),
                any(String.class),
                any(byte[].class),
                any(String.class)
        );

        System.out.println("--- CREATE SALE & EMAIL INTEGRATION TEST FINISHED SUCCESSFULLY ---");
    }
}
