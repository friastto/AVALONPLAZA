package org.frias.avalon.person.services.interfaces;

import org.frias.avalon.person.entity.Person;

public interface PersonService {
    Person findByNumberId(String numberId);
}
