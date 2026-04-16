package org.frias.avalon.domain.person.repository;


import org.frias.avalon.domain.person.entity.Person;
import org.frias.avalon.domain.user.domain.entities.UserAvalon;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PersonaRepository extends CrudRepository<Person,Long> {


    @Query("""
    SELECT u FROM UserAvalon u 
    JOIN u.person p 
    WHERE p.numberid = :numberId
    """)
    List<UserAvalon> getAllAccountsByPersonNumberId(@Param("numberId") String numberId);

    Optional<Person> findByNumberid(String numberid);
}
