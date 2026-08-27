package org.frias.avalon.domain.sale.application.usecase.sale.create;

import org.frias.avalon.core.permissions.CurrentUserProviderPort;
import org.frias.avalon.core.permissions.UserContext;
import org.frias.avalon.domain.person.infraestructure.persistence.entity.PersonEntity;
import org.frias.avalon.domain.person.infraestructure.persistence.repository.JpaPersonRepository;
import org.frias.avalon.domain.product.infraestructure.entity.ProductOutlet;
import org.frias.avalon.domain.product.infraestructure.repository.JpaProductOutletRepository;
import org.frias.avalon.domain.sale.application.dto.request.CreateSaleRequest;
import org.frias.avalon.domain.sale.application.dto.request.SaleItemRequest;
import org.frias.avalon.domain.sale.application.dto.response.SaleResponse;
import org.frias.avalon.domain.user.infraestructure.persistence.entity.UserAvalon;
import org.frias.avalon.domain.user.infraestructure.persistence.repository.JpaUserAvalonRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import org.frias.avalon.core.tenant.TenantContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
public class CreateSaleWithEmailIntegrationTest {

    @Autowired
    private CreateSaleUseCase createSaleUseCase;

    @Autowired
    private JpaPersonRepository personRepository;

    @Autowired
    private JpaUserAvalonRepository userRepository;

    @Autowired
    private JpaProductOutletRepository productRepository;

    @Autowired
    private org.frias.avalon.domain.masterdata.infraestructure.persistence.repository.JpaMasterDataRepository masterDataRepository;

    @MockBean
    private CurrentUserProviderPort currentUserProvider;

    @MockBean
    private org.frias.avalon.domain.notification.application.port.EmailSenderPort emailSenderPort;

    @Autowired
    private org.frias.avalon.core.tenant.FlywayMultiTenantService flywayMultiTenantService;

    @org.junit.jupiter.api.BeforeEach
    public void setUp() {
        flywayMultiTenantService.migrateTenantSchema("store_4");
        TenantContext.clear();
        TenantContext.setTenantOutletId(4L);
    }

    @org.junit.jupiter.api.AfterEach
    public void tearDown() {
        TenantContext.clear();
    }

    @Test
    public void testCreateSaleAndSendEmail() throws InterruptedException {
        System.out.println("--- STARTING CREATE SALE & EMAIL INTEGRATION TEST ---");

        // 1. Dynamic MasterData lookups (avoid fragile hardcoded IDs in CI)
        Long activeStatusId = masterDataRepository.findByShortName("ACT").orElseThrow().getId();
        Long ccIdentId = masterDataRepository.findByShortName("CC").orElseThrow().getId();
        Long unitMeasureId = masterDataRepository.findByShortName("UND").orElseThrow().getId();
        Long cashPaymentMethodId = masterDataRepository.findByShortName("EFE").orElseThrow().getId();

        // 2. Configurar Mock Security Context
        UserContext mockContext = new UserContext("SoporteAvalon", List.of("ROLE_ADMINTI"), 4L);
        Mockito.when(currentUserProvider.getCurrentUserContext()).thenReturn(mockContext);
        Mockito.when(currentUserProvider.getCurrentOutletId()).thenReturn(4L);
        Mockito.when(currentUserProvider.hasRole("ROLE_ADMIN")).thenReturn(true);
        Mockito.when(currentUserProvider.hasRole("ROLE_ADMINTI")).thenReturn(true);

        // 3. Crear Empleado (Person) y Usuario (UserAvalon) en la BD
        String employeeUsername = "SoporteAvalon";
        userRepository.findByUserName(employeeUsername).ifPresent(u -> userRepository.delete(u));
        
        String empDoc = "88888888";
        PersonEntity employeePerson = personRepository.findByNumberId(empDoc)
                .orElseGet(() -> {
                    PersonEntity p = new PersonEntity();
                    p.setNumberId(empDoc);
                    p.setName("Soporte");
                    p.setLastName("Avalon");
                    p.setEmail("soporte@avalon.com");
                    p.setIdentificationId(ccIdentId);
                    p.setStatusId(activeStatusId);
                    p.setCreatedAt(LocalDateTime.now());
                    return personRepository.saveAndFlush(p);
                });
        
        UserAvalon employeeUser = new UserAvalon();
        employeeUser.setUserName(employeeUsername);
        employeeUser.setHashSalt("salt");
        employeeUser.setHashPassword("password");
        employeeUser.setStatusId(activeStatusId);
        employeeUser.setPersonId(employeePerson.getId());
        employeeUser.setCreatedAt(LocalDateTime.now());
        userRepository.saveAndFlush(employeeUser);

        // 4. Crear Cliente de prueba en la BD
        String clientDoc = "999888777";
        PersonEntity client = personRepository.findByNumberId(clientDoc)
                .orElseGet(() -> {
                    PersonEntity c = new PersonEntity();
                    c.setNumberId(clientDoc);
                    c.setName("Cliente");
                    c.setLastName("Prueba Email");
                    c.setEmail("friastto@gmail.com");
                    c.setIdentificationId(ccIdentId);
                    c.setStatusId(activeStatusId);
                    c.setCreatedAt(LocalDateTime.now());
                    return personRepository.saveAndFlush(c);
                });

        // 5. Crear producto exclusivo de prueba con stock 100 en store_4
        ProductOutlet newProd = new ProductOutlet();
        newProd.setLocalName("Producto Test Email " + System.currentTimeMillis());
        newProd.setLocalDescription("Desc");
        newProd.setLocalPrice(new BigDecimal("1000.00"));
        newProd.setStock(100);
        newProd.setUnitMeasureId(unitMeasureId);
        newProd.setOutletId(4L);
        newProd.setStatusId(activeStatusId);
        newProd.setCreatedAt(LocalDateTime.now());
        ProductOutlet product = productRepository.saveAndFlush(newProd);

        // 6. Preparar peticion de venta
        SaleItemRequest itemRequest = new SaleItemRequest(product.getId(), "1");
        CreateSaleRequest saleRequest = new CreateSaleRequest(
                clientDoc,
                4L, // outletId
                cashPaymentMethodId, // paymentMethodId (Efectivo)
                new BigDecimal("1000.00"), // amountReceived
                List.of(itemRequest),
                true // sendEmail = true
        );

        // 6. Ejecutar la venta en el esquema de tienda
        SaleResponse saleResponse = createSaleUseCase.execute(saleRequest);
        assertNotNull(saleResponse);
        System.out.println("Sale created: " + saleResponse.saleCode());

        // 7. Esperar para ver si el correo se envía asíncronamente
        System.out.println("Waiting for async email listener...");
        Thread.sleep(1000);
        System.out.println("--- CREATE SALE & EMAIL INTEGRATION TEST FINISHED ---");
    }
}
