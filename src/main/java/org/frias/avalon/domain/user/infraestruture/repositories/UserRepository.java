package org.frias.avalon.domain.user.infraestruture.repositories;


import org.frias.avalon.core.tenant.config.TenantAware;
import org.frias.avalon.domain.user.domain.entities.UserAvalon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@TenantAware
@Repository
public interface UserRepository extends JpaRepository<UserAvalon, Long> {
    Optional<UserAvalon> findByUserName(String userName);
    // Esta consulta busca el usuario por la identificación de la persona asociada
    // y verifica que el usuario esté activo.

    @Query("""
        SELECT COUNT(u) > 0
        FROM UserAvalon u
        WHERE u.person.id = :personId
          AND u.rolId.id = :roleId
    """)
    boolean existsByPersonAndRole(@Param("personId") Long personId,
                                  @Param("roleId") Long roleId);


    List<UserAvalon> findAllByPersonId(Long personId);

    @Query("""
SELECT u FROM UserAvalon u
            JOIN u.rolId rol 
            JOIN MasterData subPadre ON rol.parentId = subPadre.id 
            JOIN MasterData padre ON rol.parentId = subPadre.id 
            WHERE u.person.numberid = :doc 
            AND padre.shortName = 'TYPE_EMPLOYEE' 
            AND u.person.statusId.shortName = 'ACT'
             """)// Asumiendo que el status está en Person
    Optional<UserAvalon> findActiveEmployeeByNumberId(@Param("doc") String doc);


    /*
    busca en la base de atos los roles que tiene asociado esa persona
     */
    @Query("""
        SELECT u.rolId.shortName 
        FROM UserAvalon u 
        JOIN u.person p 
        WHERE p.numberid = :numberId 
        AND u.statusId.shortName =   'ACT'
    """)
    List<String>findRolesByPersonNumberId(@Param("numberId") String numberId);


    @Query("""
        SELECT u
        FROM UserAvalon u 
        JOIN u.companyId c
        WHERE c.id = :id 
        AND u.statusId.shortName =   'ACT'
            ORDER BY u.userName ASC
        
    """)
    List<UserAvalon>getAllEmployeesOnlyCompany(@Param("id") Long id);

    @Query("""
    SELECT u.userName 
    FROM UserAvalon u 
    WHERE u.outletId.id = :id 
      AND u.statusId.shortName = 'ACT'
    ORDER BY u.userName ASC
""")
    List<UserAvalon>getAllEmployeesOnlyOutlet(@Param("id") Long id);




    @Query("""
SELECT u 
FROM UserAvalon u 
LEFT JOIN u.outletId o
WHERE u.statusId.shortName = 'ACT'
ORDER BY o.id ASC NULLS FIRST, u.userName ASC
""")
    List<UserAvalon> getAllEmployesCompany();



}
