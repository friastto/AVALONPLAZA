package org.frias.avalon.temp.features.tenant.users.repository;

import org.frias.avalon.domain.usergeneral.useravalon.entities.UserAvalon;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeCompanyRepository extends JpaRepository<UserAvalon, Long> {
}
