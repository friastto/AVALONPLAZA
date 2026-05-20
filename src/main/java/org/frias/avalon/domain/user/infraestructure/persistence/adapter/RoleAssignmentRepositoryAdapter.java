package org.frias.avalon.domain.user.infraestructure.persistence.adapter;

import org.frias.avalon.domain.user.domain.mapper.RoleAssignmentMapper;
import org.frias.avalon.domain.user.domain.model.RoleAssignmentDomain;
import org.frias.avalon.domain.user.domain.port.RoleAssignmentRepositoryPort;
import org.frias.avalon.domain.user.infraestructure.persistence.entity.RoleAssignment;
import org.frias.avalon.domain.user.infraestructure.persistence.repository.JpaRoleAssignmentRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RoleAssignmentRepositoryAdapter implements RoleAssignmentRepositoryPort {

    private final JpaRoleAssignmentRepository jpa;
    private final RoleAssignmentMapper mapper;


    public RoleAssignmentRepositoryAdapter(JpaRoleAssignmentRepository jpa, RoleAssignmentMapper mapper) {
        this.jpa = jpa;
        this.mapper = mapper;
    }

    @Override
    public RoleAssignmentDomain create(RoleAssignmentDomain domain) {

        RoleAssignment role = mapper.toEntity(domain);

        return mapper.toDomain(jpa.save(role));
    }

    @Override
    public List<RoleAssignmentDomain> findByUserAvalonId(Long id) {
        List<RoleAssignment> roleAssignment = jpa.findByUserId(id);

        return roleAssignment.stream().map(mapper::toDomain).toList();
    }
}
