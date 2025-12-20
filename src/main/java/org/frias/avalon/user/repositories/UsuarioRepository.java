package org.frias.avalon.user.repositories;


import org.frias.avalon.user.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<User, Long> {
    Optional<User> findByUserName(String userName);
    // Esta consulta busca el usuario por la identificación de la persona asociada
    // y verifica que el usuario esté activo.



    @Query("""
SELECT u FROM User u
            JOIN u.rolId rol 
            JOIN MasterData subPadre ON rol.parentId = subPadre.id 
            JOIN MasterData padre ON rol.parentId = subPadre.id 
            WHERE u.person.numberid = :doc 
            AND padre.shortName = 'TYPE_EMPLOYEE' 
            AND u.person.statusId.shortName = 'ACTIVE'
             """)// Asumiendo que el status está en Person
    Optional<User> findActiveEmployeeByNumberId(@Param("doc") String doc);

}
