package org.frias.avalon.module.masterdata.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.frias.avalon.domain.masterdata.controllers.MasterDataController;
import org.frias.avalon.domain.masterdata.dtos.request.MasterDataNewDto;
import org.frias.avalon.domain.masterdata.entities.MasterData;
import org.frias.avalon.domain.masterdata.services.interfaces.MasterDataService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class MasterDataControllerTest {

    private MockMvc mockMvc;

    @Mock
    private MasterDataService service;

    @InjectMocks
    private MasterDataController controller;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setup() {
        // Configuramos MockMvc manualmente
        this.mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    @DisplayName("GET /masterData/search/v1/{shortName} - Debe retornar status 200 y el objeto JSON")
    void shouldReturnMasterDataByShortName() throws Exception {
        // Given
        MasterData mockResult = new MasterData(1L, "ACTIVO", "ACT", null, 2L);
        given(service.searchByShortName("ACT")).willReturn(mockResult);

        // When/Then
        mockMvc.perform(get("/masterData/search/v1/ACT")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shortName").value("ACT"))
                .andExpect(jsonPath("$.fullName").value("ACTIVO"));
    }

    @Test
    @DisplayName("POST /masterData/saveAll - Debe guardar lista y retornar 200 OK")
    void shouldSaveAllMasterData() throws Exception {
        // Given
        MasterDataNewDto dto = new MasterDataNewDto("NUEVO", "NEW", null, "ACT");
        List<MasterDataNewDto> requestList = List.of(dto);
        
        MasterData savedData = new MasterData(10L, "NUEVO", "NEW", null, 2L);
        given(service.createAll(anyList())).willReturn(List.of(savedData));

        // When/Then
        mockMvc.perform(post("/masterData/saveAll")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestList)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].shortName").value("NEW"));
    }
}
