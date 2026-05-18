package org.frias.avalon.domain.person.application.usecase.create;

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
    private final MasterTreeProvider masterTreeProvider; // Para validar statusId, typeIdentificationId, sexId

    public CreatePersonUseCaseImpl(PersonRepositoryPort personRepositoryPort, PersonMapper personMapper, MasterTreeProvider masterTreeProvider) {
        this.personRepositoryPort = personRepositoryPort;
        this.personMapper = personMapper;
        this.masterTreeProvider = masterTreeProvider;
    }

    @Transactional
    @Override
    public PersonResponse execute(CreatePersonRequest request) {
        // Validaciones de existencia de MasterData (statusId, typeIdentificationId, sexId)
        // Esto asegura que los IDs proporcionados existan en el sistema de MasterData
        masterTreeProvider.getTree().getByIdOrThrow(request.statusId());
        masterTreeProvider.getTree().getByIdOrThrow(request.typeIdentificationId());
        if (request.sexId() != null) {
            masterTreeProvider.getTree().getByIdOrThrow(request.sexId());
        }

        // Crear el objeto de dominio PersonDomain usando el Factory Method
        // Las validaciones de negocio (ej. número de identificación, nombre, contacto)
        // se realizan dentro del Factory Method createBasic de PersonDomain
        PersonDomain person = PersonDomain.createBasic(
                request.typeIdentificationId(),
                request.numberid(),
                request.name(),
                request.lastName(),
                request.phoneNumber(),
                request.email(),
                request.statusId()
        );

        // Guardar la persona a través del puerto de repositorio
        PersonDomain savedPerson = personRepositoryPort.save(person);

        // Convertir el objeto de dominio guardado a un DTO de respuesta
        return personMapper.toResponse(savedPerson);
    }
}