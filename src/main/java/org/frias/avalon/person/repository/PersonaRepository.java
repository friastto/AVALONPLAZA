package org.frias.avalon.person.repository;


import org.frias.avalon.person.entity.Person;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PersonaRepository extends CrudRepository<Person,Long> {
}
