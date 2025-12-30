package org.frias.avalon.person.services.implementation;

import jakarta.persistence.EntityNotFoundException;
import org.frias.avalon.person.entity.Person;
import org.frias.avalon.person.repository.PersonaRepository;
import org.frias.avalon.person.services.interfaces.PersonService;
import org.springframework.stereotype.Service;

@Service
public class PersonServiceImpl implements PersonService {

    private final PersonaRepository personaRepository;

    public PersonServiceImpl(PersonaRepository personaRepository) {
        this.personaRepository = personaRepository;

    }

    @Override
    public Person findByNumberId(String numberId) {
        return personaRepository.findByNumberid(numberId)
                .orElseThrow(() -> new EntityNotFoundException("Cliente No registrado en el sistema"));

    }
}
