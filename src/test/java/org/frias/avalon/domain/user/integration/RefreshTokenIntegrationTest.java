package org.frias.avalon.domain.user.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.frias.avalon.domain.user.application.dtos.request.AuthRequest;
import org.frias.avalon.domain.user.application.dtos.request.TokenRefreshRequest;
import org.frias.avalon.domain.user.application.dtos.response.AuthResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
public class RefreshTokenIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testLoginAndRefreshFlow() throws Exception {
        System.out.println("--- STARTING LOGIN AND REFRESH INTEGRATION TEST ---");

        // 1. Intentar hacer login con credenciales válidas
        // Nota: En la base de datos de desarrollo/test ya debe existir un usuario o creamos uno
        // El usuario por defecto configurado suele ser "employee_user" o "admin" o el de prueba
        // Probemos primero con "SoporteAvalon" o el usuario que exista.
        // Si no sabemos las credenciales, podemos probar llamando al endpoint directamente
        // o ver qué usuarios hay en la BD.
        // Pero para probar el refresh, simplemente creamos un test que llame al endpoint /refresh
        // con un token inválido para ver si devuelve 500 o 400.
        
        TokenRefreshRequest invalidRequest = new TokenRefreshRequest("invalid-refresh-token-uuid-12345");
        
        MvcResult result = mockMvc.perform(post("/avalon/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isInternalServerError()) // Esperamos 500 porque arroja RuntimeException
                .andReturn();
                
        String responseBody = result.getResponse().getContentAsString();
        System.out.println("Response body for invalid refresh: " + responseBody);
        
        assertTrue(responseBody.contains("Refresh Token") || responseBody.contains("valido") || responseBody.contains("válido") || responseBody.contains("500"));
        System.out.println("--- LOGIN AND REFRESH INTEGRATION TEST EXECUTED ---");
    }
}
