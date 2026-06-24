package org.frias.avalon.domain.person.infraestructure.persistence.adapter;

import org.frias.avalon.domain.person.domain.model.PersonDomain;
import org.frias.avalon.domain.person.domain.port.PersonRepositoryPort;
import org.frias.avalon.domain.person.infraestructure.mapper.PersonMapper;
import org.frias.avalon.domain.person.infraestructure.persistence.entity.PersonEntity;
import org.frias.avalon.domain.person.infraestructure.persistence.repository.JpaPersonRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class PersonRepositoryAdapter implements PersonRepositoryPort {

    private final JpaPersonRepository jpaPersonRepository;
    private final PersonMapper personMapper;

    public PersonRepositoryAdapter(JpaPersonRepository jpaPersonRepository, PersonMapper personMapper) {
        this.jpaPersonRepository = jpaPersonRepository;
        this.personMapper = personMapper;
    }

    @Override
    public PersonDomain save(PersonDomain personDomain) {
        PersonEntity person = personMapper.toEntity(personDomain);
        PersonEntity personSaved = jpaPersonRepository.save(person);
        return personMapper.toDomain(personSaved);
    }

    @Override
    public Optional<PersonDomain> findById(Long id) {
        return jpaPersonRepository.findById(id).map(personMapper::toDomain);
    }

    @Override
    public Optional<PersonDomain> findByNumberid(String identificationNumber) {
        return jpaPersonRepository.findByNumberId(identificationNumber).map(personMapper::toDomain);
    }

}