package org.frias.avalon.domain.company.integration.presentation;

import org.frias.avalon.core.jwt.service.JwtTokenProviderPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableAutoConfiguration(exclude = {MailSenderAutoConfiguration.class})
@Transactional
@DisplayName("Pruebas de Integracion E2E - Company Dashboard")
class CompanyDashboardIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JwtTokenProviderPort jwtTokenProvider;

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        UserDetails userDetails = User.builder()
                .username("superadmin_test")
                .password("password")
                .authorities("ROLE_GERGEN", "GERGEN", "ROLE_ADMINSYS", "ADMINSYS")
                .build();
        String token = jwtTokenProvider.generateAccessToken(userDetails, 1L);
        headers.setBearerAuth(token);
        return headers;
    }

    @Test
    @DisplayName("Deberia obtener el tablero financiero consolidado de la empresa exitosamente (E2E)")
    void getCompanyDashboard_DefaultPeriod_Success() {
        HttpEntity<Void> entity = new HttpEntity<>(createHeaders());

        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                "/api/v1/companies/1/dashboard?period=MES",
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<>() {}
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(200, response.getBody().get("status"));

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
        assertNotNull(data);
        assertEquals(1, ((Number) data.get("companyId")).longValue());
        assertEquals("MES", data.get("period"));
        assertNotNull(data.get("totalSales"));
        assertNotNull(data.get("netProfit"));
        assertNotNull(data.get("profitMarginPercentage"));
        assertNotNull(data.get("salesByPaymentMethod"));
        assertNotNull(data.get("outletSales"));
    }

    @Test
    @DisplayName("Deberia filtrar tablero financiero por sede especifica exitosamente (E2E)")
    void getCompanyDashboard_WithOutletFilter_Success() {
        HttpEntity<Void> entity = new HttpEntity<>(createHeaders());

        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                "/api/v1/companies/1/dashboard?period=HOY&outletId=4",
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<>() {}
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
        assertNotNull(data);
        assertEquals(1, ((Number) data.get("companyId")).longValue());
        assertEquals("HOY", data.get("period"));
        assertEquals(4, ((Number) data.get("selectedOutletId")).longValue());
    }

    @Test
    @DisplayName("Deberia obtener tablero con periodo HISTORICO exitosamente (E2E)")
    void getCompanyDashboard_HistoricalPeriod_Success() {
        HttpEntity<Void> entity = new HttpEntity<>(createHeaders());

        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                "/api/v1/companies/1/dashboard?period=HISTORICO",
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<>() {}
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
        assertNotNull(data);
        assertEquals("HISTORICO", data.get("period"));
        assertNull(data.get("selectedOutletId"));
    }
}
