package org.frias.avalon.domain.user.infraestruture.persistence.repository;


import aj.org.objectweb.asm.commons.Remapper;
import org.frias.avalon.domain.user.infraestruture.persistence.entity.UserAvalon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface JpaUserAvalonRepository extends JpaRepository<UserAvalon, Long> {

    Optional<UserAvalon> findByUserName(String userName);




}
