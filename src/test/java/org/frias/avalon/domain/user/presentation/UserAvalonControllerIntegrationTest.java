package org.frias.avalon.domain.user.presentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.frias.avalon.BaseIntegrationTest;
import org.frias.avalon.domain.masterdata.domain.repository.MasterDataRepositoryPort;
import org.frias.avalon.domain.user.application.dtos.request.UserNewDto;
import org.frias.avalon.domain.user.domain.model.UserAvalonDomain;
import org.frias.avalon.domain.user.domain.port.UserAvalonRepositoryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

class UserAvalonControllerIntegrationTest extends BaseIntegrationTest {


    private final MockMvc mockMvc;


    private final ObjectMapper objectMapper; // Para convertir objetos a JSON

    private final UserAvalonRepositoryPort userAvalonRepositoryPort; // Para verificar el estado de la DB


    private final MasterDataRepositoryPort masterDataRepositoryPort; // Para obtener statusId

    UserAvalonControllerIntegrationTest(MockMvc mockMvc, ObjectMapper objectMapper, UserAvalonRepositoryPort userAvalonRepositoryPort, MasterDataRepositoryPort masterDataRepositoryPort) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
        this.userAvalonRepositoryPort = userAvalonRepositoryPort;
        this.masterDataRepositoryPort = masterDataRepositoryPort;
    }

    @Test
    @DisplayName("should create a new user successfully via API")
    void shouldCreateNewUserSuccessfullyViaApi() throws Exception {
        Long activeStatusId = masterDataRepositoryPort.findByCode("ACT").orElseThrow().getId();
        UserNewDto newUser = new UserNewDto("apiuser", "securepassword");

        mockMvc.perform(MockMvcRequestBuilders.post("/avalon/user/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUser)))
                .andDo(print()) // Imprime la petición y respuesta en consola
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code").value(201))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("se creo el usuario exitosamente"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.userName").value("apiuser"));

        // Verificar que el usuario fue guardado en la base de datos
        assertThat(userAvalonRepositoryPort.findByUserName("apiuser")).isPresent();
    }

    @Test
    @DisplayName("should return conflict when creating user with existing username")
    void shouldReturnConflictWhenCreatingUserWithExistingUsername() throws Exception {
        Long activeStatusId = masterDataRepositoryPort.findByCode("ACT").orElseThrow().getId();

        // Crear un usuario primero para que exista
        UserAvalonDomain existingUser = UserAvalonDomain.create("existinguser", "salt", "hashedpass", activeStatusId);
        userAvalonRepositoryPort.save(existingUser);

        // Intentar crear otro usuario con el mismo username
        UserNewDto duplicateUser = new UserNewDto("existinguser", "anotherpassword");

        mockMvc.perform(MockMvcRequestBuilders.post("/avalon/user/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateUser)))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().isBadRequest()) // Asumiendo que un DomainValidationException se mapea a 400 Bad Request
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Ya existe un usuario con el nombre de usuario"));
    }

    @Test
    @DisplayName("should get all users successfully")
    void shouldGetAllUsersSuccessfully() throws Exception {
        Long activeStatusId = masterDataRepositoryPort.findByCode("ACT").orElseThrow().getId();

        // Asegurarse de que haya al menos un usuario
        UserAvalonDomain user1 = UserAvalonDomain.create("user1", "salt1", "pass1", activeStatusId);
        UserAvalonDomain user2 = UserAvalonDomain.create("user2", "salt2", "pass2", activeStatusId);
        userAvalonRepositoryPort.save(user1);
        userAvalonRepositoryPort.save(user2);

        mockMvc.perform(MockMvcRequestBuilders.get("/avalon/user/all")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code").value(200))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data").isArray())
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.length()").value(2));
    }
}