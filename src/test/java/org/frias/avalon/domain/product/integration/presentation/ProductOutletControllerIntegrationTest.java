package org.frias.avalon.domain.product.integration.presentation;

import org.frias.avalon.domain.masterdata.domain.repository.MasterDataRepositoryPort;
import org.frias.avalon.domain.product.application.dto.request.ProductNewDataRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

import org.springframework.transaction.annotation.Transactional;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
            "app.jwt.secret=supersecretjwtkeythatisatleast32characterslong",
            "app.jwt.expiration-ms=3600000",
            "app.jwt.refresh-expiration-ms=86400000",
            "aws.accessKey=mock-access-key",
            "aws.secretKey=mock-secret-key",
            "aws.region=us-east-1",
            "removebg.apikey=mock-removebg-key",
            "spring.sql.init.mode=never" 
        })
@Transactional
@DisplayName("Pruebas de Integración E2E - ProductOutletController")
class ProductOutletControllerIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private MasterDataRepositoryPort masterDataRepositoryPort;

    @Autowired
    private org.frias.avalon.core.jwt.service.JwtTokenProviderPort jwtTokenProvider;

    @Autowired
    private org.frias.avalon.core.tenant.FlywayMultiTenantService flywayMultiTenantService;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        flywayMultiTenantService.migrateTenantSchema("store_4");
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        org.springframework.security.core.userdetails.UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                .username("admin_test")
                .password("password")
                .authorities("ROLE_GERENTE", "GERENTE")
                .build();
        String token = jwtTokenProvider.generateAccessToken(userDetails, 4L);
        headers.setBearerAuth(token);
        return headers;
    }

    @Test
    @DisplayName("Flujo Completo: Debería crear un Producto correctamente (E2E)")
    void createProduct_EndToEnd() {
        // Arrange
        // Buscamos un ID real de unidad (ej. "KG") dinámicamente desde la BD de pruebas
        Long unitId = masterDataRepositoryPort.getIdByCode("KG");
        assertNotNull(unitId, "El ID de la unidad 'KG' debe existir en la BD");

        String uniqueBarcode = String.valueOf(System.currentTimeMillis());
        ProductNewDataRequest requestDto = new ProductNewDataRequest(
                uniqueBarcode,
                "Producto Prueba E2E",
                "Descripción de prueba",
                "2.5", // 2.5 KG
                unitId,
                "http://imagen.url",
                new BigDecimal("150.00"),
                4L // outletId valido de pruebas
        );

        HttpEntity<ProductNewDataRequest> requestEntity = new HttpEntity<>(requestDto, createHeaders());

        // Act
        ResponseEntity<String> response = restTemplate.postForEntity("/avalon/products/create", requestEntity, String.class);
        System.out.println("Product response status: " + response.getStatusCode() + ", body: " + response.getBody());

        // Assert
        assertTrue(response.getStatusCode() == HttpStatus.OK || response.getStatusCode() == HttpStatus.CREATED, "Debería retornar 200 OK o 201 CREATED");
        assertTrue(response.getBody().contains("Producto Prueba E2E"), "El cuerpo debe contener el nombre del producto");
        assertTrue(response.getBody().contains("2.5 KG"), "El cuerpo debe contener el stock formateado por el servicio de conversión");
    }

    @Test
    @DisplayName("Flujo Completo: Debería fallar la validación si el DTO es inválido (E2E)")
    void createProduct_ValidationError_EndToEnd() {
        // Arrange
        // Precio negativo y nombre vacío para disparar @Valid
        ProductNewDataRequest invalidDto = new ProductNewDataRequest(
                "",
                "", 
                "Desc",
                "1.0",
                1L,
                "url",
                new BigDecimal("-10.0"), 
                1L
        );
        HttpEntity<ProductNewDataRequest> requestEntity = new HttpEntity<>(invalidDto, createHeaders());

        // Act
        ResponseEntity<String> response = restTemplate.postForEntity("/avalon/products/create", requestEntity, String.class);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode(), "Debería retornar 400 Bad Request por fallo de @Valid");
    }
}
