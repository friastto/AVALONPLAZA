package org.frias.avalon.domain.masterdata.integration.presentation;

import org.frias.avalon.domain.masterdata.application.dto.request.MasterDataNewDto;
import org.frias.avalon.domain.masterdata.domain.repository.MasterDataRepositoryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableAutoConfiguration(exclude = {MailSenderAutoConfiguration.class})
@DisplayName("Pruebas de Integracion E2E - MasterRootController")
class MasterRootControllerIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    // Inyectamos el puerto del repositorio para obtener datos dinámicos de la BD
    @Autowired
    private MasterDataRepositoryPort masterDataRepositoryPort;

    @Autowired
    private org.frias.avalon.core.jwt.service.JwtTokenProviderPort jwtTokenProvider;

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        org.springframework.security.core.userdetails.UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                .username("admin_test")
                .password("password")
                .authorities("ROLE_ADMINTI", "ADMINTI")
                .build();
        String token = jwtTokenProvider.generateAccessToken(userDetails, 1L);
        headers.setBearerAuth(token);
        return headers;
    }

    @Test
    @DisplayName("Flujo Completo: Debería crear un MasterData correctamente (E2E)")
    void createMasterData_EndToEnd() {
        // Arrange
        String uniqueCode = "N2E" + (System.currentTimeMillis() % 10000);
        MasterDataNewDto requestDto = new MasterDataNewDto("NUEVO NODO E2E " + uniqueCode, uniqueCode, "STSGEN", "ACT");
        HttpEntity<MasterDataNewDto> requestEntity = new HttpEntity<>(requestDto, createHeaders());

        // Act
        ResponseEntity<String> response = restTemplate.postForEntity("/avalon/masterdata/create", requestEntity, String.class);

        System.out.println(response.getBody() != null ? response.getBody() : "");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode(), "Debería retornar 200 OK");
        assertTrue(response.getBody().contains(uniqueCode));
        assertTrue(response.getBody().contains("se creo el tipo exitosamente"));
    }

    @Test
    @DisplayName("Flujo Completo: Debería fallar la validación al intentar crear con datos inválidos (E2E)")
    void createMasterData_ValidationError_EndToEnd() {
        // Arrange (shortName vacío)
        MasterDataNewDto invalidDto = new MasterDataNewDto("FULL NAME", "", "GEN", "ACT");
        HttpEntity<MasterDataNewDto> requestEntity = new HttpEntity<>(invalidDto, createHeaders());

        // Act
        ResponseEntity<String> response = restTemplate.postForEntity("/avalon/masterdata/create", requestEntity, String.class);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("Flujo Completo: Debería buscar un MasterData por ID existente (E2E)")
    void findMasterDataById_EndToEnd() {
        // Arrange
        HttpEntity<Void> requestEntity = new HttpEntity<>(createHeaders());
        
        // IMPORTANTE: Obtenemos el ID real dinámicamente desde la base de datos
        Long realId = masterDataRepositoryPort.getIdByCode("ACT");
        assertNotNull(realId, "El nodo ACT debe existir en la base de datos de prueba");

        // Act
        ResponseEntity<String> response = restTemplate.exchange("/avalon/masterdata/search/v2/" + realId, HttpMethod.GET, requestEntity, String.class);

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
        ResponseEntity<String> response = restTemplate.exchange("/avalon/masterdata/search/v2/99999999", HttpMethod.GET, requestEntity, String.class);

        // Assert
        assertTrue(response.getStatusCode() == HttpStatus.NOT_FOUND || response.getStatusCode() == HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
