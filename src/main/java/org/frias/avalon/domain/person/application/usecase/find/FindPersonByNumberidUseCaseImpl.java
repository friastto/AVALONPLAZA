package org.frias.avalon.domain.person.application.usecase.find;

import jakarta.persistence.EntityNotFoundException;
import org.frias.avalon.domain.person.application.dto.response.PersonResponse;
import org.frias.avalon.domain.person.domain.model.PersonDomain;
import org.frias.avalon.domain.person.domain.port.PersonRepositoryPort;
import org.frias.avalon.domain.person.infraestructure.mapper.PersonMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FindPersonByNumberidUseCaseImpl implements FindPersonByNumberidUseCase {

    private final PersonRepositoryPort personRepositoryPort;
    private final PersonMapper personMapper;

    public FindPersonByNumberidUseCaseImpl(PersonRepositoryPort personRepositoryPort, PersonMapper personMapper) {
        this.personRepositoryPort = personRepositoryPort;
        this.personMapper = personMapper;
    }

    @Transactional(readOnly = true) // Es una operación de solo lectura
    @Override
    public PersonResponse execute(String numberid) {
        // Buscar la persona por su número de identificación a través del puerto de repositorio
        PersonDomain person = personRepositoryPort.findByNumberid(numberid)
                .orElseThrow(() -> new EntityNotFoundException("Persona no encontrada con número de identificación: " + numberid));

        // Convertir el objeto de dominio encontrado a un DTO de respuesta
        return personMapper.toResponse(person);
    }
}