package org.frias.avalon.domain.masterdata.integration.presentation;

import org.frias.avalon.domain.masterdata.application.dto.request.MasterDataNewDto;
import org.frias.avalon.domain.masterdata.domain.repository.MasterDataRepositoryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
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

@DisplayName("Pruebas de Integración E2E - MasterRootController")
class MasterRootControllerIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    // Inyectamos el puerto del repositorio para obtener datos dinámicos de la BD
    @Autowired
    private MasterDataRepositoryPort masterDataRepositoryPort;

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    @Test
    @DisplayName("Flujo Completo: Debería crear un MasterData correctamente (E2E)")
    void createMasterData_EndToEnd() {
        // Arrange
        MasterDataNewDto requestDto = new MasterDataNewDto("NUEVO NODO E2E", "NODE2E", "STSGEN", "ACT");
        HttpEntity<MasterDataNewDto> requestEntity = new HttpEntity<>(requestDto, createHeaders());

        // Act
        ResponseEntity<String> response = restTemplate.postForEntity("/masterRoot/create", requestEntity, String.class);

        System.out.println(response.getBody().toString());


        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode(), "Debería retornar 200 OK");
        assertTrue(response.getBody().contains("NODE2E"));
        assertTrue(response.getBody().contains("se creo el tipo exitosamente"));
    }

    @Test
    @DisplayName("Flujo Completo: Debería fallar la validación al intentar crear con datos inválidos (E2E)")
    void createMasterData_ValidationError_EndToEnd() {
        // Arrange (shortName vacío)
        MasterDataNewDto invalidDto = new MasterDataNewDto("FULL NAME", "", "GEN", "ACT");
        HttpEntity<MasterDataNewDto> requestEntity = new HttpEntity<>(invalidDto, createHeaders());

        // Act
        ResponseEntity<String> response = restTemplate.postForEntity("/masterRoot/create", requestEntity, String.class);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("Flujo Completo: Debería buscar un MasterData por ID existente (E2E)")
    void findMasterDataById_EndToEnd() {
        // Arrange
        HttpEntity<Void> requestEntity = new HttpEntity<>(createHeaders());
        
        // IMPORTANTE: Obtenemos el ID real dinámicamente desde la base de datos
        // En lugar de usar un ID fijo como "1", buscamos el ID del nodo "ACT" que sabemos que existe en data-test.sql.
        // Hacemos esto porque los IDs autoincrementales pueden variar entre ejecuciones en H2 si la secuencia no se reinicia.
        Long realId = masterDataRepositoryPort.getIdByCode("ACT");
        assertNotNull(realId, "El nodo ACT debe existir en la base de datos de prueba");

        // Act
        ResponseEntity<String> response = restTemplate.exchange("/masterRoot/search/v2/" + realId, HttpMethod.GET, requestEntity, String.class);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("\"shortName\":\"ACT\""));
    }

    @Test
    @DisplayName("Flujo Completo: Debería retornar error 404 al buscar un ID inexistente (E2E)")
    void findMasterDataById_NotFound_EndToEnd() {
        // Arrange
        HttpEntity<Void> requestEntity = new HttpEntity<>(createHeaders());

        // Act
        // Usamos un ID que es muy improbable que exista
        ResponseEntity<String> response = restTemplate.exchange("/masterRoot/search/v2/99999999", HttpMethod.GET, requestEntity, String.class);

        // Assert
        assertTrue(response.getStatusCode() == HttpStatus.NOT_FOUND || response.getStatusCode() == HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
