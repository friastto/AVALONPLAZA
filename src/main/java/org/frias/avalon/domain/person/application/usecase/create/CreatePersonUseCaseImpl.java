package org.frias.avalon.domain.person.application.usecase.create;

import org.frias.avalon.domain.masterdata.domain.model.MasterRoot;
import org.frias.avalon.domain.masterdata.domain.service.MasterTreeProvider;
import org.frias.avalon.domain.person.application.dto.request.CreatePersonRequest;
import org.frias.avalon.domain.person.application.dto.response.PersonResponse;
import org.frias.avalon.domain.person.domain.model.PersonDomain;
import org.frias.avalon.domain.person.domain.port.PersonRepositoryPort;
import org.frias.avalon.domain.person.infraestructure.mapper.PersonMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreatePersonUseCaseImpl implements CreatePersonUseCase {

    private final PersonRepositoryPort personRepositoryPort;
    private final PersonMapper personMapper;
    private final MasterTreeProvider masterTreeProvider;

    public CreatePersonUseCaseImpl(PersonRepositoryPort personRepositoryPort, PersonMapper personMapper, MasterTreeProvider masterTreeProvider) {
        this.personRepositoryPort = personRepositoryPort;
        this.personMapper = personMapper;
        this.masterTreeProvider = masterTreeProvider;
    }

    @Transactional
    @Override
    public PersonResponse execute(CreatePersonRequest request) {
        var tree = masterTreeProvider.getTree();

        Long statusId = request.statusId();
        if (statusId == null && request.statusCode() != null && !request.statusCode().isBlank()) {
            MasterRoot statusNode = tree.getByCode(request.statusCode().trim().toUpperCase());
            if (statusNode == null) {
                throw new IllegalArgumentException("Estado no encontrado: " + request.statusCode());
            }
            statusId = statusNode.getId();
        }
        if (statusId == null) {
            statusId = tree.getByCodeOrThrow("ACT").getId();
        }
        tree.getByIdOrThrow(statusId);

        Long typeId = request.typeIdentificationId();
        if (typeId == null && request.typeIdentificationCode() != null && !request.typeIdentificationCode().isBlank()) {
            MasterRoot typeNode = tree.getByCode(request.typeIdentificationCode().trim().toUpperCase());
            if (typeNode == null) {
                throw new IllegalArgumentException("Tipo de identificacion no encontrado: " + request.typeIdentificationCode());
            }
            typeId = typeNode.getId();
        }
        if (typeId == null) {
            throw new IllegalArgumentException("El tipo de identificacion es requerido (id o codigo)");
        }
        tree.getByIdOrThrow(typeId);

        Long sexId = request.sexId();
        if (sexId == null && request.sexCode() != null && !request.sexCode().isBlank()) {
            MasterRoot sexNode = tree.getByCode(request.sexCode().trim().toUpperCase());
            if (sexNode != null) {
                sexId = sexNode.getId();
            }
        }
        if (sexId != null) {
            tree.getByIdOrThrow(sexId);
        }

        // Crear el objeto de dominio PersonDomain usando el Factory Method (incluyendo la direccion)
        PersonDomain person = PersonDomain.createBasic(
                typeId,
                request.numberid(),
                request.name(),
                request.lastName(),
                request.address(),
                sexId,
                request.phoneNumber(),
                request.email(),
                statusId
        );

        // Guardar la persona a traves del puerto de repositorio
        PersonDomain savedPerson = personRepositoryPort.save(person);

        // Convertir el objeto de dominio guardado a un DTO de respuesta
        return personMapper.toResponse(savedPerson);
    }
}