package org.frias.avalon.domain.masterdata.presentation.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.frias.avalon.core.jwt.config.CustomUserDetailsService;
import org.frias.avalon.core.jwt.service.JwtTokenProviderPort;
import org.frias.avalon.domain.masterdata.application.dto.request.MasterDataNewDto;
import org.frias.avalon.domain.masterdata.application.dto.response.MasterDataResponseDto;
import org.frias.avalon.domain.masterdata.application.usecase.changestatus.ChangeStatusUseCase;
import org.frias.avalon.domain.masterdata.application.usecase.create.CreateAllMasterDataUseCase;
import org.frias.avalon.domain.masterdata.application.usecase.create.CreateMasterDataUseCase;
import org.frias.avalon.domain.masterdata.application.usecase.find.FindAllMasterDataUseCase;
import org.frias.avalon.domain.masterdata.application.usecase.find.FindMasterDataByIdUseCase;
import org.frias.avalon.domain.masterdata.domain.service.MasterTreeProvider;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean; 
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.CoreMatchers.is;


@WebMvcTest(MasterRootController.class)
class MasterRootControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CreateMasterDataUseCase createUseCase;
    @MockitoBean
    private FindMasterDataByIdUseCase findByIdUseCase;
    @MockitoBean
    private ChangeStatusUseCase changeStatusUseCase;
    @MockitoBean
    private CreateAllMasterDataUseCase createAllMasterDataUseCase;
    @MockitoBean
    private FindAllMasterDataUseCase findAllUseCase;

    // Mocks de dependencias de seguridad
    @MockitoBean
    private JwtTokenProviderPort jwtTokenProviderPort;
    @MockitoBean
    private MasterTreeProvider masterTreeProvider;
    @MockitoBean
    private CustomUserDetailsService userDetailsService;


    @Test
    @DisplayName("POST /masterRoot/create - Debería crear un MasterData y retornar 200 OK con los datos")
    @WithMockUser // Simula un usuario autenticado para que la seguridad no falle por token JWT
    void shouldCreateMasterDataAndReturnOk() throws Exception {
        // Arrange (Given)
        MasterDataNewDto requestDto = new MasterDataNewDto("FULL NAME", "SHORT", "PARENT", "ACT");
        MasterDataResponseDto responseDto = new MasterDataResponseDto(1L, "SHORT", "FULL NAME");

        given(createUseCase.execute(any(MasterDataNewDto.class))).willReturn(1L);
        given(findByIdUseCase.execute(1L)).willReturn(responseDto);

        // Act (When)
        ResultActions response = mockMvc.perform(post("/masterRoot/create")
                .with(csrf()) // Añade un token CSRF válido simulado a la petición para evitar error 403
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)));

        // Assert (Then)
        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(201)))
                .andExpect(jsonPath("$.message", is("se creo el tipo exitosamente")))
                .andExpect(jsonPath("$.data.id", is(1)))
                .andExpect(jsonPath("$.data.shortName", is("SHORT")));
    }

    @Test
    @DisplayName("POST /masterRoot/create - Debería retornar 400 Bad Request si el DTO es inválido")
    @WithMockUser
    void shouldReturnBadRequestWhenCreateWithInvalidDto() throws Exception {
        // Arrange (Given)
        // shortName está en blanco, lo que debería fallar la validación @NotBlank
        MasterDataNewDto invalidRequestDto = new MasterDataNewDto("FULL NAME", "", "PARENT", "ACT");

        // Act (When)
        ResultActions response = mockMvc.perform(post("/masterRoot/create")
                .with(csrf()) // Añade un token CSRF válido
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequestDto)));

        // Assert (Then)
        response.andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /masterRoot/search/v2/{id} - Debería retornar un MasterData si el ID existe")
    @WithMockUser // Para las peticiones GET, CSRF no se exige por defecto en Spring Security
    void shouldReturnMasterDataWhenIdExists() throws Exception {
        // Arrange (Given)
        Long existingId = 1L;
        MasterDataResponseDto responseDto = new MasterDataResponseDto(existingId, "SHORT", "FULL NAME");
        given(findByIdUseCase.execute(existingId)).willReturn(responseDto);

        // Act (When)
        ResultActions response = mockMvc.perform(get("/masterRoot/search/v2/{id}", existingId));

        // Assert (Then)
        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(200)))
                .andExpect(jsonPath("$.data.id", is(1)))
                .andExpect(jsonPath("$.data.shortName", is("SHORT")));
    }

    @Test
    @DisplayName("GET /masterRoot/search/v2/{id} - Debería lanzar excepción si el ID no existe (manejado por ExceptionHandler)")
    @WithMockUser
    void shouldThrowExceptionWhenIdDoesNotExist() throws Exception {
        // Arrange (Given)
        Long nonExistingId = 99L;
        // Simulamos el comportamiento del caso de uso, que lanzaría una excepción
        given(findByIdUseCase.execute(nonExistingId)).willThrow(new jakarta.persistence.EntityNotFoundException("MasterData no encontrado: " + nonExistingId));

        // Act (When)
        ResultActions response = mockMvc.perform(get("/masterRoot/search/v2/{id}", nonExistingId));

        // Assert (Then)
        // Asumimos que tienes un @ControllerAdvice que maneja EntityNotFoundException y devuelve un 404.
        response.andExpect(status().isNotFound()); 
    }
}