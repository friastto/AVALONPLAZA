package org.frias.avalon.domain.usergeneral.usercompany.repository;

import org.frias.avalon.domain.usergeneral.useravalon.entities.UserAvalon;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeCompanyRepository extends JpaRepository<UserAvalon, Long> {
}
