package org.frias.avalon.domain.person.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.frias.avalon.core.exeptions.GlobalExceptionHandler;
import org.frias.avalon.core.exeptions.ResourceNotFoundException;
import org.frias.avalon.domain.masterdata.application.dto.response.MasterDataResponseDto;
import org.frias.avalon.domain.person.application.dto.request.CreatePersonRequest;
import org.frias.avalon.domain.person.application.dto.request.VerifyIdentificationRequestDto;
import org.frias.avalon.domain.person.application.dto.response.PersonDetailResponseDto;
import org.frias.avalon.domain.person.application.dto.response.PersonResponse;
import org.frias.avalon.domain.person.application.dto.response.VerificationResponseDto;
import org.frias.avalon.domain.person.application.usecase.changestatus.ChangePersonStatusUseCase;
import org.frias.avalon.domain.person.application.usecase.create.CreatePersonUseCase;
import org.frias.avalon.domain.person.application.usecase.find.FindPersonByDocumentUseCase;
import org.frias.avalon.domain.person.application.usecase.verify.VerifyIdentificationUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("Standalone MockMvc Unit Tests for PersonController")
class PersonControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private CreatePersonUseCase createPersonUseCase;

    @Mock
    private ChangePersonStatusUseCase changeStatusUseCase;

    @Mock
    private VerifyIdentificationUseCase verifyIdentificationUseCase;

    @Mock
    private FindPersonByDocumentUseCase findPersonByDocumentUseCase;

    @BeforeEach
    void setUp() {
        PersonController controller = new PersonController(
                createPersonUseCase,
                changeStatusUseCase,
                verifyIdentificationUseCase,
                findPersonByDocumentUseCase
        );

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private PersonResponse buildSamplePersonResponse() {
        MasterDataResponseDto typeId = new MasterDataResponseDto(1L, "CC", "Cedula");
        MasterDataResponseDto sex = new MasterDataResponseDto(1L, "MAS", "Masculino");
        MasterDataResponseDto status = new MasterDataResponseDto(1L, "ACT", "Activo");

        return new PersonResponse(
                1L, "12345678", "JUAN", "PEREZ", "CALLE 123",
                typeId, sex, 5550000L, "juan@email.com", status,
                LocalDateTime.now(), LocalDateTime.now()
        );
    }

    @Test
    @DisplayName("POST /avalon/person/create - Should create person successfully and return 201 Created")
    void shouldCreatePersonSuccessfullyAndReturn201() throws Exception {
        CreatePersonRequest request = new CreatePersonRequest(
                1L,
                "12345678",
                "JUAN",
                "PEREZ",
                "CALLE 123",
                1L,
                5550000L,
                "juan@email.com",
                1L
        );

        PersonResponse expectedResponse = buildSamplePersonResponse();
        given(createPersonUseCase.execute(any(CreatePersonRequest.class))).willReturn(expectedResponse);

        mockMvc.perform(post("/avalon/person/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status", is(201)))
                .andExpect(jsonPath("$.message", is("Persona creada exitosamente")))
                .andExpect(jsonPath("$.data.id", is(1)))
                .andExpect(jsonPath("$.data.name", is("JUAN")));
    }

    @Test
    @DisplayName("POST /avalon/person/create - Should return 400 Bad Request when request is invalid")
    void shouldReturn400BadRequestWhenCreatePersonRequestIsInvalid() throws Exception {
        CreatePersonRequest invalidRequest = new CreatePersonRequest(
                null,
                "",
                "",
                "",
                "CALLE 123",
                1L,
                5550000L,
                "invalid-email",
                1L
        );

        mockMvc.perform(post("/avalon/person/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /avalon/person/{idPerson}/change/statusTo/{idStatus} - Should change status successfully and return 201 Created")
    void shouldChangeStatusSuccessfullyAndReturn201() throws Exception {
        PersonResponse expectedResponse = buildSamplePersonResponse();
        given(changeStatusUseCase.execute(eq(1L), eq(2L))).willReturn(expectedResponse);

        mockMvc.perform(post("/avalon/person/{idPerson}/change/statusTo/{idStatus}", 1L, 2L))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status", is(201)))
                .andExpect(jsonPath("$.message", is("Persona creada exitosamente")))
                .andExpect(jsonPath("$.data.id", is(1)));
    }

    @Test
    @DisplayName("POST /avalon/person/verify-identification - Should verify identification successfully and return 200 OK")
    void shouldVerifyIdentificationSuccessfullyAndReturn200() throws Exception {
        VerifyIdentificationRequestDto request = new VerifyIdentificationRequestDto("12345678");
        VerificationResponseDto responseDto = new VerificationResponseDto(true, true, "JUAN PEREZ");

        given(verifyIdentificationUseCase.execute(any(VerifyIdentificationRequestDto.class))).willReturn(responseDto);

        mockMvc.perform(post("/avalon/person/verify-identification")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(200)))
                .andExpect(jsonPath("$.message", is("Verificación completada con éxito")))
                .andExpect(jsonPath("$.data.personExists", is(true)))
                .andExpect(jsonPath("$.data.userExists", is(true)));
    }

    @Test
    @DisplayName("GET /avalon/person/by-document/{numberid} - Should find person by document successfully and return 200 OK")
    void shouldFindPersonByDocumentSuccessfullyAndReturn200() throws Exception {
        PersonDetailResponseDto detailDto = new PersonDetailResponseDto(
                true, true, 1L, "JUAN", "PEREZ", "CALLE 123", "juan@email.com", 5550000L,
                1L, "CC", 1L, "Masculino", 10L, "jualon", true, "ADMIN", 1L, 1L, "Tienda Principal", 100L
        );

        given(findPersonByDocumentUseCase.execute("12345678")).willReturn(detailDto);

        mockMvc.perform(get("/avalon/person/by-document/{numberid}", "12345678"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(200)))
                .andExpect(jsonPath("$.message", is("Consulta de persona por documento completada")))
                .andExpect(jsonPath("$.data.name", is("JUAN")));
    }

    @Test
    @DisplayName("GET /avalon/person/by-document/{numberid} - Should return 404 Not Found when document does not exist")
    void shouldReturn404NotFoundWhenDocumentDoesNotExist() throws Exception {
        given(findPersonByDocumentUseCase.execute("99999999"))
                .willThrow(new ResourceNotFoundException("Persona no encontrada con documento: 99999999"));

        mockMvc.perform(get("/avalon/person/by-document/{numberid}", "99999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)));
    }
}
