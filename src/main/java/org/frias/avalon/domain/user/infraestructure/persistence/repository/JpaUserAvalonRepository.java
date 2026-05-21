package org.frias.avalon.domain.user.infraestructure.persistence.repository;


import org.frias.avalon.domain.user.infraestructure.persistence.entity.UserAvalon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface JpaUserAvalonRepository extends JpaRepository<UserAvalon, Long> {

    Optional<UserAvalon> findByUserName(String userName);

    @Query("""
            SELECT u FROM UserAvalon u 
                        JOIN PersonEntity p ON u.personId = p.id 
                        WHERE p.numberId = :numberId
            """)
    Optional<UserAvalon> findByPersonNumberid(@Param("numberId") String numberid);

    @Query("""
            SELECT u FROM UserAvalon  u 
                        LEFT JOIN PersonEntity p ON u.personId = p.id 
                        WHERE u.userName = :identifier
                        OR p.email = :identifier
                        OR p.numberId = :identifier
            """)
    Optional<UserAvalon> findByIdentifier(@Param("identifier") String identifier);

}
