package org.frias.avalon.domain.company.integration.presentation;

import org.frias.avalon.core.jwt.service.JwtTokenProviderPort;
import org.frias.avalon.domain.company.infrastructure.entity.CompanyEntity;
import org.frias.avalon.domain.company.infrastructure.repository.JpaCompanyRepository;
import org.frias.avalon.domain.masterdata.domain.repository.MasterDataRepositoryPort;
import org.frias.avalon.domain.outlet.infraestructure.entities.Outlet;
import org.frias.avalon.domain.outlet.infraestructure.repository.JpaOutletRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableAutoConfiguration(exclude = {MailSenderAutoConfiguration.class})
@DisplayName("Pruebas de Integracion E2E - Company Dashboard")
class CompanyDashboardIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JwtTokenProviderPort jwtTokenProvider;

    @Autowired
    private JpaCompanyRepository companyRepository;

    @Autowired
    private JpaOutletRepository outletRepository;

    @Autowired
    private MasterDataRepositoryPort masterDataRepositoryPort;

    private Long testCompanyId;
    private Long testOutletId;
    private boolean createdCompany = false;
    private boolean createdOutlet = false;

    @BeforeEach
    void setUp() {
        Long actStatusId = masterDataRepositoryPort.getIdByCode("ACT");
        if (actStatusId == null) {
            actStatusId = 1L;
        }

        CompanyEntity company = companyRepository.findAll().stream().findFirst().orElse(null);
        if (company == null) {
            CompanyEntity newCompany = CompanyEntity.builder()
                    .nit("NIT-TEST-" + System.currentTimeMillis())
                    .name("Test Company E2E")
                    .email("companytest@avalon.com")
                    .statusId(actStatusId)
                    .defaultCashThresholdAmount(new BigDecimal("1000.00"))
                    .build();
            company = companyRepository.save(newCompany);
            createdCompany = true;
        }
        testCompanyId = company.getId();

        final Long currentCompanyId = testCompanyId;
        final Long finalActStatusId = actStatusId;
        final String currentNit = company.getNit();

        Outlet outlet = outletRepository.findByCompanyId(testCompanyId).stream().findFirst().orElse(null);
        if (outlet == null) {
            Outlet newOutlet = Outlet.builder()
                    .code("OUT-" + System.currentTimeMillis())
                    .nit(currentNit)
                    .name("Test Outlet E2E")
                    .address("Calle Test 123")
                    .companyId(currentCompanyId)
                    .statusId(finalActStatusId)
                    .cashThresholdAmount(new BigDecimal("500.00"))
                    .build();
            outlet = outletRepository.save(newOutlet);
            createdOutlet = true;
        }
        testOutletId = outlet.getId();
    }

    @AfterEach
    void tearDown() {
        if (createdOutlet && testOutletId != null) {
            outletRepository.deleteById(testOutletId);
        }
        if (createdCompany && testCompanyId != null) {
            companyRepository.deleteById(testCompanyId);
        }
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        UserDetails userDetails = User.builder()
                .username("superadmin_test")
                .password("password")
                .authorities("ROLE_GERGEN", "GERGEN", "ROLE_ADMINSYS", "ADMINSYS")
                .build();
        String token = jwtTokenProvider.generateAccessToken(userDetails, testCompanyId);
        headers.setBearerAuth(token);
        return headers;
    }

    @Test
    @DisplayName("Deberia obtener el tablero financiero consolidado de la empresa exitosamente (E2E)")
    void getCompanyDashboard_DefaultPeriod_Success() {
        HttpEntity<Void> entity = new HttpEntity<>(createHeaders());

        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                "/api/v1/companies/" + testCompanyId + "/dashboard?period=MES",
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
        assertEquals(testCompanyId, ((Number) data.get("companyId")).longValue());
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
                "/api/v1/companies/" + testCompanyId + "/dashboard?period=HOY&outletId=" + testOutletId,
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<>() {}
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
        assertNotNull(data);
        assertEquals(testCompanyId, ((Number) data.get("companyId")).longValue());
        assertEquals("HOY", data.get("period"));
        assertEquals(testOutletId, ((Number) data.get("selectedOutletId")).longValue());
    }

    @Test
    @DisplayName("Deberia obtener tablero con periodo HISTORICO exitosamente (E2E)")
    void getCompanyDashboard_HistoricalPeriod_Success() {
        HttpEntity<Void> entity = new HttpEntity<>(createHeaders());

        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                "/api/v1/companies/" + testCompanyId + "/dashboard?period=HISTORICO",
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<>() {}
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
        assertNotNull(data);
        assertEquals(testCompanyId, ((Number) data.get("companyId")).longValue());
        assertEquals("HISTORICO", data.get("period"));
        assertNull(data.get("selectedOutletId"));
    }

    @Test
    @DisplayName("Deberia conmutar contexto usando headers X-Company-Id y X-Outlet-Id exitosamente (E2E)")
    void getCompanyDashboard_WithCompanyAndOutletHeaders_Success() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        UserDetails userDetails = User.builder()
                .username("adminti_test")
                .password("password")
                .authorities("ROLE_ADMINTI", "ADMINTI")
                .build();
        String token = jwtTokenProvider.generateAccessToken(userDetails, null, null);
        headers.setBearerAuth(token);
        headers.set("X-Company-Id", String.valueOf(testCompanyId));
        headers.set("X-Outlet-Id", String.valueOf(testOutletId));

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                "/api/v1/companies/" + testCompanyId + "/dashboard?period=MES",
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<>() {}
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(200, response.getBody().get("status"));
    }
}
