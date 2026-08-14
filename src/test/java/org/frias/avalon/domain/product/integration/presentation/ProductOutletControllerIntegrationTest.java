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

@DisplayName("Pruebas de Integración E2E - ProductOutletController")
class ProductOutletControllerIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private MasterDataRepositoryPort masterDataRepositoryPort;

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    @Test
    @DisplayName("Flujo Completo: Debería crear un Producto correctamente (E2E)")
    void createProduct_EndToEnd() {
        // Arrange
        // Buscamos un ID real de unidad (ej. "KG") dinámicamente desde la BD de pruebas
        Long unitId = masterDataRepositoryPort.getIdByCode("KG");
        assertNotNull(unitId, "El ID de la unidad 'KG' debe existir en la BD");

        ProductNewDataRequest requestDto = new ProductNewDataRequest(
                "1234567890123",
                "Producto Prueba E2E",
                "Descripción de prueba",
                "2.5", // 2.5 KG
                unitId,
                "http://imagen.url",
                new BigDecimal("150.00"),
                10L // outletId ficticio, en el futuro debería validarse
        );

        HttpEntity<ProductNewDataRequest> requestEntity = new HttpEntity<>(requestDto, createHeaders());

        // Act
        ResponseEntity<String> response = restTemplate.postForEntity("/avalon/products/create", requestEntity, String.class);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode(), "Debería retornar 201 CREATED");
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
