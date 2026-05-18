package org.frias.avalon.domain.person.domain.port;

import org.frias.avalon.domain.person.domain.model.PersonDomain;

import java.util.Optional;

public interface PersonRepositoryPort {

    PersonDomain save(PersonDomain person);

    Optional<PersonDomain> findById(Long id);

    Optional<PersonDomain> findByNumberid(String numberid);

    // Puedes añadir más métodos según las necesidades del dominio
    // List<PersonDomain> findAll();
    // void deleteById(Long id);
}