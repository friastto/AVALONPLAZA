package org.frias.avalon.domain.person.infraestructure.mapper;

import org.frias.avalon.domain.masterdata.application.dto.response.MasterDataResponseDto;
import org.frias.avalon.domain.masterdata.domain.model.MasterRoot;
import org.frias.avalon.domain.masterdata.domain.model.MasterTree;
import org.frias.avalon.domain.masterdata.domain.service.MasterTreeProvider;
import org.frias.avalon.domain.masterdata.infraestructure.mapper.MasterDataMapperService;
import org.frias.avalon.domain.person.application.dto.response.PersonResponse;
import org.frias.avalon.domain.person.domain.model.PersonDomain;
import org.frias.avalon.domain.person.infraestructure.persistence.entity.PersonEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Unit Tests for PersonMapper")
class PersonMapperTest {

    @Mock
    private MasterTreeProvider treeProvider;

    @Mock
    private MasterDataMapperService masterDataMapperService;

    @Mock
    private MasterTree masterTree;

    private PersonMapper personMapper;

    @BeforeEach
    void setUp() {
        personMapper = new PersonMapper(treeProvider, masterDataMapperService);
    }

    private PersonDomain createSampleDomain(Long sexId) {
        return PersonDomain.createFromEntity(
                1L, "12345678", "JUAN", "PEREZ", "CALLE 123",
                10L, sexId, 5550000L, "juan@email.com", 20L,
                LocalDateTime.now(), LocalDateTime.now()
        );
    }

    private PersonEntity createSampleEntity() {
        return PersonEntity.builder()
                .id(1L)
                .numberId("12345678")
                .name("JUAN")
                .lastName("PEREZ")
                .address("CALLE 123")
                .identificationId(10L)
                .sexId(2L)
                .phoneNumber(5550000L)
                .email("juan@email.com")
                .statusId(20L)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Nested
    @DisplayName("toEntity Mapping Tests")
    class ToEntityTests {

        @Test
        @DisplayName("Should return null when domain is null")
        void shouldReturnNullWhenDomainIsNull() {
            assertNull(personMapper.toEntity(null));
        }

        @Test
        @DisplayName("Should map PersonDomain to PersonEntity correctly")
        void shouldMapDomainToEntity() {
            PersonDomain domain = createSampleDomain(2L);
            PersonEntity entity = personMapper.toEntity(domain);

            assertNotNull(entity);
            assertEquals(1L, entity.getId());
            assertEquals("12345678", entity.getNumberId());
            assertEquals("JUAN", entity.getName());
            assertEquals("PEREZ", entity.getLastName());
            assertEquals("CALLE 123", entity.getAddress());
            assertEquals(10L, entity.getIdentificationId());
            assertEquals(2L, entity.getSexId());
            assertEquals(5550000L, entity.getPhoneNumber());
            assertEquals("juan@email.com", entity.getEmail());
            assertEquals(20L, entity.getStatusId());
        }
    }

    @Nested
    @DisplayName("toDomain Mapping Tests")
    class ToDomainTests {

        @Test
        @DisplayName("Should return null when entity is null")
        void shouldReturnNullWhenEntityIsNull() {
            assertNull(personMapper.toDomain(null));
        }

        @Test
        @DisplayName("Should map PersonEntity to PersonDomain correctly")
        void shouldMapEntityToDomain() {
            PersonEntity entity = createSampleEntity();
            PersonDomain domain = personMapper.toDomain(entity);

            assertNotNull(domain);
            assertEquals(1L, domain.getId());
            assertEquals("12345678", domain.getNumberid());
            assertEquals("JUAN", domain.getName());
            assertEquals("PEREZ", domain.getLastName());
            assertEquals("CALLE 123", domain.getAddress());
            assertEquals(10L, domain.getTypeIdentificationId());
            assertEquals(2L, domain.getSexId());
            assertEquals(5550000L, domain.getPhoneNumber());
            assertEquals("juan@email.com", domain.getEmail());
            assertEquals(20L, domain.getStatusId());
        }
    }

    @Nested
    @DisplayName("toResponse Mapping Tests")
    class ToResponseTests {

        @Test
        @DisplayName("Should return null when domain is null")
        void shouldReturnNullWhenDomainIsNullForResponse() {
            assertNull(personMapper.toResponse(null));
        }

        @Test
        @DisplayName("Should map PersonDomain to PersonResponse with non-null sexId")
        void shouldMapDomainToResponseWithSex() {
            PersonDomain domain = createSampleDomain(2L);

            when(treeProvider.getTree()).thenReturn(masterTree);

            MasterRoot idRoot = new MasterRoot(10L, "CC", "Cedula", 0L, 1L);
            MasterRoot sexRoot = new MasterRoot(2L, "MAS", "Masculino", 0L, 1L);
            MasterRoot statusRoot = new MasterRoot(20L, "ACT", "Activo", 0L, 1L);

            when(masterTree.getByIdOrThrow(10L)).thenReturn(idRoot);
            when(masterTree.getByIdOrThrow(2L)).thenReturn(sexRoot);
            when(masterTree.getByIdOrThrow(20L)).thenReturn(statusRoot);

            MasterDataResponseDto idDto = new MasterDataResponseDto(10L, "CC", "Cedula");
            MasterDataResponseDto sexDto = new MasterDataResponseDto(2L, "MAS", "Masculino");
            MasterDataResponseDto statusDto = new MasterDataResponseDto(20L, "ACT", "Activo");

            when(masterDataMapperService.toResponse(idRoot)).thenReturn(idDto);
            when(masterDataMapperService.toResponse(sexRoot)).thenReturn(sexDto);
            when(masterDataMapperService.toResponse(statusRoot)).thenReturn(statusDto);

            PersonResponse response = personMapper.toResponse(domain);

            assertNotNull(response);
            assertEquals(1L, response.id());
            assertEquals("12345678", response.numberid());
            assertNotNull(response.typeIdentification());
            assertEquals("Cedula", response.typeIdentification().fullName());
            assertNotNull(response.sex());
            assertEquals("Masculino", response.sex().fullName());
            assertNotNull(response.status());
            assertEquals("Activo", response.status().fullName());
        }

        @Test
        @DisplayName("Should map PersonDomain to PersonResponse with null sexId")
        void shouldMapDomainToResponseWithNullSex() {
            PersonDomain domain = createSampleDomain(null);

            when(treeProvider.getTree()).thenReturn(masterTree);

            MasterRoot idRoot = new MasterRoot(10L, "CC", "Cedula", 0L, 1L);
            MasterRoot statusRoot = new MasterRoot(20L, "ACT", "Activo", 0L, 1L);

            when(masterTree.getByIdOrThrow(10L)).thenReturn(idRoot);
            when(masterTree.getByIdOrThrow(20L)).thenReturn(statusRoot);

            MasterDataResponseDto idDto = new MasterDataResponseDto(10L, "CC", "Cedula");
            MasterDataResponseDto statusDto = new MasterDataResponseDto(20L, "ACT", "Activo");

            when(masterDataMapperService.toResponse(idRoot)).thenReturn(idDto);
            when(masterDataMapperService.toResponse(statusRoot)).thenReturn(statusDto);

            PersonResponse response = personMapper.toResponse(domain);

            assertNotNull(response);
            assertEquals(1L, response.id());
            assertNull(response.sex());
            assertNotNull(response.typeIdentification());
            assertNotNull(response.status());
        }
    }
}
