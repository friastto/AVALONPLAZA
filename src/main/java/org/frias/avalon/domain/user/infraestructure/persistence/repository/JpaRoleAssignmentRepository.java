package org.frias.avalon.domain.user.infraestructure.persistence.repository;

import org.frias.avalon.domain.user.infraestructure.persistence.entity.RoleAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JpaRoleAssignmentRepository extends JpaRepository<RoleAssignment, Long> {
    List<RoleAssignment> findByUserId(Long id);
}
