package org.frias.avalon.domain.person.infraestructure.mapper;

import lombok.RequiredArgsConstructor;
import org.frias.avalon.domain.masterdata.application.dto.response.MasterDataResponseDto;
import org.frias.avalon.domain.masterdata.domain.model.MasterRoot;
import org.frias.avalon.domain.masterdata.domain.model.MasterTree;
import org.frias.avalon.domain.masterdata.domain.service.MasterTreeProvider;
import org.frias.avalon.domain.masterdata.infraestructure.mapper.MasterDataMapperService;
import org.frias.avalon.domain.person.application.dto.response.PersonResponse;
import org.frias.avalon.domain.person.domain.model.PersonDomain;
import org.frias.avalon.domain.person.infraestructure.persistence.entity.PersonEntity;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor // Lombok para inyección de dependencias en el constructor
public class PersonMapper {

    private final MasterTreeProvider treeProvider;
    private final MasterDataMapperService masterDataMapperService; // Inyectar MasterDataMapperService

    public PersonEntity toEntity(PersonDomain domain) {
        if (domain == null) {
            return null;
        }
        return PersonEntity.builder()
                .id(domain.getId())
                .numberId(domain.getNumberid())
                .name(domain.getName())
                .lastName(domain.getLastName())
                .address(domain.getAddress())
                .identificationId(domain.getTypeIdentificationId()) // Corregido a typeIdentificationId
                .sexId(domain.getSexId())
                .phoneNumber(domain.getPhoneNumber())
                .email(domain.getEmail())
                .statusId(domain.getStatusId())
                // createdAt y updatedAt son gestionados por @PrePersist/@PreUpdate en la entidad
                .build();
    }

    public PersonDomain toDomain(PersonEntity entity) {
        if (entity == null) {
            return null;
        }
        // Usamos el Factory Method del dominio para reconstruir el objeto
        return PersonDomain.createFromEntity(
                entity.getId(),
                entity.getNumberId(),
                entity.getName(),
                entity.getLastName(),
                entity.getAddress(),
                entity.getIdentificationId(), // Corregido a typeIdentificationId
                entity.getSexId(),
                entity.getPhoneNumber(),
                entity.getEmail(),
                entity.getStatusId(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    // Método toResponse para mapear PersonDomain a PersonResponse
    public PersonResponse toResponse(PersonDomain domain) {
        if (domain == null) {
            return null;
        }

        MasterTree masterTree = treeProvider.getTree();

        // Obtener y mapear MasterData para typeIdentification
        MasterRoot typeIdentificationMasterRoot = masterTree.getByIdOrThrow(domain.getTypeIdentificationId());
        MasterDataResponseDto typeIdentificationDto = masterDataMapperService.toResponse(typeIdentificationMasterRoot);

        // Obtener y mapear MasterData para sex (puede ser nulo)
        MasterDataResponseDto sexDto = null;
        if (domain.getSexId() != null) {
            MasterRoot sexMasterRoot = masterTree.getByIdOrThrow(domain.getSexId());
            sexDto = masterDataMapperService.toResponse(sexMasterRoot);
        }

        // Obtener y mapear MasterData para status
        MasterRoot statusMasterRoot = masterTree.getByIdOrThrow(domain.getStatusId());
        MasterDataResponseDto statusDto = masterDataMapperService.toResponse(statusMasterRoot);

        return new PersonResponse(
                domain.getId(),
                domain.getNumberid(),
                domain.getName(),
                domain.getLastName(),
                domain.getAddress(),
                typeIdentificationDto,
                sexDto,
                domain.getPhoneNumber(),
                domain.getEmail(),
                statusDto,
                domain.getCreatedAt(),
                domain.getUpdatedAt()
        );
    }
}