package org.frias.avalon.person.services.implementation;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import org.frias.avalon.maestra.services.interfaces.MasterDataService;
import org.frias.avalon.person.dto.PersonRequestNewDto;
import org.frias.avalon.person.entity.Person;
import org.frias.avalon.person.repository.PersonaRepository;
import org.frias.avalon.person.services.interfaces.PersonService;
import org.springframework.stereotype.Service;

@Service
public class PersonServiceImpl implements PersonService {

    private final PersonaRepository personaRepository;
    private final MasterDataService masterDataService;

    public PersonServiceImpl(PersonaRepository personaRepository, MasterDataService masterDataService) {
        this.personaRepository = personaRepository;

        this.masterDataService = masterDataService;
    }

    @Override
    public Person findByNumberId(String numberId) {
        return personaRepository.findByNumberid(numberId)
                .orElseThrow(() -> new EntityNotFoundException("No registrado en el sistema : "+numberId));

    }

    @Override
    public Person save(PersonRequestNewDto personCreate) {

        Person person = new Person();

        personaRepository.findByNumberid(personCreate.numberId())
                .ifPresent(p -> {
                    throw new EntityExistsException("la identificación del usuario no está disponible");
                });

           person.setNumberid(personCreate.numberId());
           person.setName(personCreate.name());
           person.setDir(personCreate.address());
           person.setLastName(personCreate.lastName());
           person.setIdentificationId(masterDataService.findById(personCreate.identificationId()));
           person.setStatusId(masterDataService.searchShortName("ACT"));
           person.setSexId(masterDataService.findById(personCreate.sexId()));

           return personaRepository.save(person);

    }

    @Override
    public Person searchById(Long id) {

        return personaRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Persona no encontrada en el sistema : para añadirle un susario a una persona debe crear una persona y despues asignarselo al usuario"));
    }
}
