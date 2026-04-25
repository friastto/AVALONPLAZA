package org.frias.avalon.module.masterdata.controller;

import org.frias.avalon.domain.masterdata.infraestructure.persistence.repository.MasterDataRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@Sql("/data-h2.sql")
class MasterDataIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private MasterDataRepository repository;

    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        // Configuramos MockMvc manualmente sin usar la anotación @AutoConfigureMockMvc
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext).build();
    }

    @Test
    @DisplayName("GET /masterData/search/v1/ACT - Debe devolver status 200 y el objeto JSON")
    void shouldReturnMasterDataByShortName() throws Exception {
        
        // El script data-h2.sql inserta 'ACT' con ID 2
        mockMvc.perform(get("/masterData/search/v1/ACT")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shortName").value("ACT"))
                .andExpect(jsonPath("$.fullName").value("ACTIVO"));
    }

    @Test
    @DisplayName("GET /masterData/search/v1/UNKNOWN - Debe devolver error 404 o 500")
    void shouldFailIfNotFound() throws Exception {
        
        // Probamos con un valor que no existe
        mockMvc.perform(get("/masterData/search/v1/UNKNOWN_CODE")
                        .contentType(MediaType.APPLICATION_JSON))
                // Como tu servicio lanza EntityNotFoundException, Spring Boot por defecto devuelve 500 o 404 
                .andExpect(status().is5xxServerError()); 
    }
}
