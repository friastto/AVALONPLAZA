package org.frias.avalon.domain.roleassignment.infraestructure;

import org.frias.avalon.domain.roleassignment.domain.entity.RoleAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleAssignmentRepository extends JpaRepository<RoleAssignment,Long> {
}
