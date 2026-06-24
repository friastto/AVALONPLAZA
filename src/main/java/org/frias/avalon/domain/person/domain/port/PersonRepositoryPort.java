package org.frias.avalon.domain.person.domain.port;

import org.frias.avalon.domain.person.domain.model.PersonDomain;

import java.util.Optional;

public interface PersonRepositoryPort {
    PersonDomain save(PersonDomain personDomain);
    Optional<PersonDomain> findById(Long id);

    Optional<PersonDomain> findByNumberid(String numberid);
}