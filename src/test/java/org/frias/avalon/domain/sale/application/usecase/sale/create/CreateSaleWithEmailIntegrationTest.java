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

@SpringBootTest
public class CreateSaleWithEmailIntegrationTest {

    @Autowired
    private CreateSaleUseCase createSaleUseCase;

    @Autowired
    private JpaPersonRepository personRepository;

    @Autowired
    private JpaUserAvalonRepository userRepository;

    @Autowired
    private JpaProductOutletRepository productRepository;

    @MockBean
    private CurrentUserProviderPort currentUserProvider;

    @Test
    public void testCreateSaleAndSendEmail() throws InterruptedException {
        System.out.println("--- STARTING CREATE SALE & EMAIL INTEGRATION TEST ---");

        // 1. Configurar Mock Security Context
        UserContext mockContext = new UserContext("SoporteAvalon", List.of("ROLE_ADMINTI"), 4L);
        Mockito.when(currentUserProvider.getCurrentUserContext()).thenReturn(mockContext);
        Mockito.when(currentUserProvider.getCurrentOutletId()).thenReturn(4L);
        Mockito.when(currentUserProvider.hasRole("ROLE_ADMIN")).thenReturn(true);
        Mockito.when(currentUserProvider.hasRole("ROLE_ADMINTI")).thenReturn(true);

        // 2. Crear Empleado (Person) y Usuario (UserAvalon) en la BD para que no falle al buscar el usuario autenticado
        String employeeUsername = "SoporteAvalon";
        userRepository.findByUserName(employeeUsername).ifPresent(u -> userRepository.delete(u));
        
        PersonEntity employeePerson = new PersonEntity();
        employeePerson.setNumberId("88888888");
        employeePerson.setName("Soporte");
        employeePerson.setLastName("Avalon");
        employeePerson.setEmail("soporte@avalon.com");
        employeePerson.setIdentificationId(135L); // Válido
        employeePerson.setStatusId(1L); // ACT
        employeePerson.setCreatedAt(LocalDateTime.now());
        personRepository.saveAndFlush(employeePerson);
        
        UserAvalon employeeUser = new UserAvalon();
        employeeUser.setUserName(employeeUsername);
        employeeUser.setHashSalt("salt");
        employeeUser.setHashPassword("password");
        employeeUser.setStatusId(1L); // ACT
        employeeUser.setPersonId(employeePerson.getId());
        employeeUser.setCreatedAt(LocalDateTime.now());
        userRepository.saveAndFlush(employeeUser);

        // 3. Crear Cliente de prueba con un correo real en la BD real de desarrollo
        String clientDoc = "999888777";
        personRepository.findByNumberId(clientDoc).ifPresent(p -> personRepository.delete(p));
        
        PersonEntity client = new PersonEntity();
        client.setNumberId(clientDoc);
        client.setName("Cliente");
        client.setLastName("Prueba Email");
        client.setEmail("friastto@gmail.com"); // Enviamos a este correo de prueba
        client.setIdentificationId(135L); // Cédula de ciudadanía u otro válido en tu masterData
        client.setStatusId(1L); // ACT
        client.setCreatedAt(LocalDateTime.now());
        personRepository.saveAndFlush(client);

        // 4. Crear Producto de prueba en la BD (o buscar uno existente en outlet 4L)
        List<ProductOutlet> products = productRepository.findAll();
        ProductOutlet product = products.stream()
                .filter(p -> p.getOutletId().equals(4L))
                .findFirst()
                .orElseGet(() -> {
                    ProductOutlet newProd = new ProductOutlet();
                    newProd.setLocalName("Producto Test Email");
                    newProd.setLocalDescription("Desc");
                    newProd.setLocalPrice(new BigDecimal("1000.00"));
                    newProd.setStock(50);
                    newProd.setUnitMeasureId(22L); // UNIDAD validadas en masterData
                    newProd.setOutletId(4L);
                    newProd.setStatusId(1L);
                    newProd.setCreatedAt(LocalDateTime.now());
                    return productRepository.saveAndFlush(newProd);
                });

        // 5. Preparar petición de venta
        SaleItemRequest itemRequest = new SaleItemRequest(product.getId(), "1");
        CreateSaleRequest saleRequest = new CreateSaleRequest(
                clientDoc,
                4L, // outletId
                139L, // paymentMethodId (Efectivo)
                new BigDecimal("1000.00"), // amountReceived
                List.of(itemRequest),
                true // sendEmail = true
        );

        // 6. Ejecutar la venta
        SaleResponse saleResponse = createSaleUseCase.execute(saleRequest);
        assertNotNull(saleResponse);
        System.out.println("Sale created: " + saleResponse.saleCode());

        // 7. Esperar 15 segundos para ver si el correo se envía asíncronamente
        System.out.println("Waiting for async email listener...");
        Thread.sleep(15000);
        System.out.println("--- CREATE SALE & EMAIL INTEGRATION TEST FINISHED ---");
    }
}
