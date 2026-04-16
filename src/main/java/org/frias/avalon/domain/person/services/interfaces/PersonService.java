package org.frias.avalon.domain.person.services.interfaces;

import org.frias.avalon.domain.person.dto.PersonRequestNewDto;
import org.frias.avalon.domain.person.entity.Person;

import java.util.Optional;

public interface PersonService {
    Optional<Person> findByNumberId(String numberId);
    Person save(PersonRequestNewDto personCreate);
    Person searchById(Long id);

    Person create(
            String numberId,
                  String name,
                  String lastName,
                  String address,
                  Long identificationId,
                  Long sexId
    );
}
