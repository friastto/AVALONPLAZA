package org.frias.avalon.domain.product.integration.presentation;

import org.frias.avalon.core.jwt.service.JwtTokenProviderPort;
import org.frias.avalon.core.tenant.FlywayMultiTenantService;
import org.frias.avalon.domain.masterdata.domain.repository.MasterDataRepositoryPort;
import org.frias.avalon.domain.company.infrastructure.entity.CompanyEntity;
import org.frias.avalon.domain.company.infrastructure.repository.JpaCompanyRepository;
import org.frias.avalon.domain.outlet.infraestructure.entities.Outlet;
import org.frias.avalon.domain.outlet.infraestructure.repository.JpaOutletRepository;
import org.frias.avalon.domain.product.application.dto.request.ProductNewDataRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableAutoConfiguration(exclude = {MailSenderAutoConfiguration.class})
@DisplayName("Pruebas de Integracion E2E - ProductOutletController")
class ProductOutletControllerIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private MasterDataRepositoryPort masterDataRepositoryPort;

    @Autowired
    private JwtTokenProviderPort jwtTokenProvider;

    @Autowired
    private FlywayMultiTenantService flywayMultiTenantService;

    @Autowired
    private JpaOutletRepository jpaOutletRepository;

    @Autowired
    private JpaCompanyRepository companyRepository;

    private Long testOutletId;

    @BeforeEach
    void setUp() {
        Long rawStatusId = masterDataRepositoryPort.getIdByCode("ACT");
        final Long actStatusId = (rawStatusId != null) ? rawStatusId : 1L;

        CompanyEntity company = companyRepository.findAll().stream().findFirst().orElseGet(() -> {
            CompanyEntity newCompany = CompanyEntity.builder()
                    .nit("NIT-TEST-" + System.currentTimeMillis())
                    .name("Test Company E2E")
                    .email("companytest@avalon.com")
                    .statusId(actStatusId)
                    .defaultCashThresholdAmount(new BigDecimal("1000.00"))
                    .build();
            return companyRepository.save(newCompany);
        });

        final Long companyId = company.getId();

        Outlet outlet = jpaOutletRepository.findAll().stream().findFirst().orElseGet(() -> {
            Outlet newOutlet = new Outlet();
            newOutlet.setName("Tienda Test E2E");
            newOutlet.setAddress("Calle Test 123");
            newOutlet.setCompanyId(companyId);
            newOutlet.setStatusId(actStatusId);
            return jpaOutletRepository.save(newOutlet);
        });
        testOutletId = outlet.getId();
        flywayMultiTenantService.migrateTenantSchema("store_" + testOutletId);
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        UserDetails userDetails = User.builder()
                .username("admin_test")
                .password("password")
                .authorities("ROLE_GERENTE", "GERENTE")
                .build();
        String token = jwtTokenProvider.generateAccessToken(userDetails, testOutletId);
        headers.setBearerAuth(token);
        return headers;
    }

    @Test
    @DisplayName("Flujo Completo: Deberia crear un Producto correctamente (E2E)")
    void createProduct_EndToEnd() {
        // Arrange
        Long unitId = masterDataRepositoryPort.getIdByCode("KG");
        assertNotNull(unitId, "El ID de la unidad 'KG' debe existir en la BD");

        String uniqueBarcode = String.valueOf(System.currentTimeMillis());
        ProductNewDataRequest requestDto = new ProductNewDataRequest(
                uniqueBarcode,
                "Producto Prueba E2E",
                "Descripcion de prueba",
                "2.5", // 2.5 KG
                unitId,
                "http://imagen.url",
                new BigDecimal("150.00"),
                testOutletId
        );

        HttpEntity<ProductNewDataRequest> requestEntity = new HttpEntity<>(requestDto, createHeaders());

        // Act
        ResponseEntity<String> response = restTemplate.postForEntity("/avalon/products/create", requestEntity, String.class);
        System.out.println("Product response status: " + response.getStatusCode() + ", body: " + response.getBody());

        // Assert
        assertTrue(response.getStatusCode() == HttpStatus.OK || response.getStatusCode() == HttpStatus.CREATED, "Deberia retornar 200 OK o 201 CREATED");
        assertTrue(response.getBody().contains("Producto Prueba E2E"), "El cuerpo debe contener el nombre del producto");
        assertTrue(response.getBody().contains("2.5 KG"), "El cuerpo debe contener el stock formateado por el servicio de conversion");
    }

    @Test
    @DisplayName("Flujo Completo: Deberia fallar la validacion si el DTO es invalido (E2E)")
    void createProduct_ValidationError_EndToEnd() {
        // Arrange
        ProductNewDataRequest invalidDto = new ProductNewDataRequest(
                "",
                "", 
                "Desc",
                "1.0",
                1L,
                "url",
                new BigDecimal("-10.0"), 
                testOutletId
        );
        HttpEntity<ProductNewDataRequest> requestEntity = new HttpEntity<>(invalidDto, createHeaders());

        // Act
        ResponseEntity<String> response = restTemplate.postForEntity("/avalon/products/create", requestEntity, String.class);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode(), "Deberia retornar 400 Bad Request por fallo de @Valid");
    }
}
