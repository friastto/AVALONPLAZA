package org.frias.avalon.domain.person.infraestructure.persistence.repository;

import org.frias.avalon.domain.person.infraestructure.persistence.entity.PersonEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JpaPersonRepository extends JpaRepository<PersonEntity, Long> {

    Optional<PersonEntity> findByNumberId(String numberId);

    Optional<PersonEntity> findByIdentificationIdAndNumberId(Long identificationId, String numberId);

    @Query("""
             SELECT p FROM PersonEntity p 
             JOIN UserAvalon u ON u.personId = p.id 
             WHERE u.userName = :userName
            """)
    Optional<PersonEntity> findByUserUsername(@Param("userName") String userName);


}