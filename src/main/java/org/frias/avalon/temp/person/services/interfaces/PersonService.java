package org.frias.avalon.temp.person.services.interfaces;

import org.frias.avalon.temp.person.dto.PersonRequestNewDto;
import org.frias.avalon.temp.person.entity.Person;

public interface PersonService {
    Person findByNumberId(String numberId);
    Person save(PersonRequestNewDto personCreate);
    Person searchById(Long id);
}
