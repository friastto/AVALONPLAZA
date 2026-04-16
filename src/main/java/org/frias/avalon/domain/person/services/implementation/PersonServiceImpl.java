package org.frias.avalon.domain.person.services.implementation;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import org.frias.avalon.domain.masterdata.services.interfaces.MasterDataService;
import org.frias.avalon.domain.person.dto.PersonRequestNewDto;
import org.frias.avalon.domain.person.entity.Person;
import org.frias.avalon.domain.person.repository.PersonaRepository;
import org.frias.avalon.domain.person.services.interfaces.PersonService;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PersonServiceImpl implements PersonService {

    private final PersonaRepository personaRepository;
    private final MasterDataService masterDataService;

    public PersonServiceImpl(PersonaRepository personaRepository, MasterDataService masterDataService) {
        this.personaRepository = personaRepository;

        this.masterDataService = masterDataService;
    }

    @Override
    public Optional<Person> findByNumberId(String numberId) {
        return personaRepository.findByNumberid(numberId);


    }

    @Override
    public Person save(PersonRequestNewDto personCreate) {

        return create(personCreate.numberId(), personCreate.name(),personCreate.lastName(),personCreate.address(),personCreate.identificationId(),personCreate.sexId());
    }

    @Override
    public Person searchById(Long id) {

        return personaRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Persona no encontrada en el sistema : para añadirle un susario a una persona debe crear una persona y despues asignarselo al usuario"));
    }

    @Override
    public Person create(String numberId, String name, String lastName, String address, Long identificationId, Long sexId) {
        Person person = new Person();

        personaRepository.findByNumberid(numberId)
                .ifPresent(p -> {
                    throw new EntityExistsException("la identificación del usuario no está disponible");
                });

        person.setNumberid(numberId);
        person.setName(name);
        person.setDir(address);
        person.setLastName(lastName);
        person.setIdentificationId(masterDataService.searchById(identificationId));
        person.setStatusId(masterDataService.getStatusActive());
        person.setSexId(masterDataService.searchById(sexId));

        return personaRepository.save(person);
    }
}
