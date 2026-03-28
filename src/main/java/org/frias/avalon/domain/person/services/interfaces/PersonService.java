package org.frias.avalon.domain.person.services.interfaces;

import org.frias.avalon.domain.person.dto.PersonRequestNewDto;
import org.frias.avalon.domain.person.entity.Person;

public interface PersonService {
    Person findByNumberId(String numberId);
    Person save(PersonRequestNewDto personCreate);
    Person searchById(Long id);
}
