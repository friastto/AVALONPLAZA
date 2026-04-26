package org.frias.avalon.domain.user.infraestruture.persistence.repository;

import org.frias.avalon.domain.user.infraestruture.persistence.entity.RoleAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaRoleAssignmentRepository extends JpaRepository<RoleAssignment,Long> {
}
