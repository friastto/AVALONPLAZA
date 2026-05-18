package org.frias.avalon.domain.person.infraestructure.persistence.adapter;

import org.frias.avalon.domain.person.domain.model.PersonDomain;
import org.frias.avalon.domain.person.domain.port.PersonRepositoryPort;
import org.frias.avalon.domain.person.infraestructure.mapper.PersonMapper;
import org.frias.avalon.domain.person.infraestructure.persistence.entity.PersonEntity;
import org.frias.avalon.domain.person.infraestructure.persistence.repository.JpaPersonRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class PersonPersistenceAdapter implements PersonRepositoryPort {

    private final JpaPersonRepository jpaPersonRepository;
    private final PersonMapper personMapper;

    public PersonPersistenceAdapter(JpaPersonRepository jpaPersonRepository, PersonMapper personMapper) {
        this.jpaPersonRepository = jpaPersonRepository;
        this.personMapper = personMapper;
    }

    @Override
    public PersonDomain save(PersonDomain person) {
        PersonEntity personEntity = personMapper.toEntity(person);
        PersonEntity savedEntity = jpaPersonRepository.save(personEntity);
        return personMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<PersonDomain> findById(Long id) {
        return jpaPersonRepository.findById(id)
                .map(personMapper::toDomain);
    }

    @Override
    public Optional<PersonDomain> findByNumberid(String numberid) {
        return jpaPersonRepository.findByNumberId(numberid)
                .map(personMapper::toDomain);
    }
}