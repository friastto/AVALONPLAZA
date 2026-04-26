package org.frias.avalon.domain.user.infraestruture.persistence.repository;


import org.frias.avalon.domain.user.infraestruture.persistence.entity.UserAvalon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface JpaUserAvalonRepository extends JpaRepository<UserAvalon, Long> {


}
