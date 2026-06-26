package org.frias.avalon.domain.person.application.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record PersonDetailResponseDto(

        // ── Existencia ───────────────────────────────────
        Boolean personExists,
        Boolean userExists,

        // ── Datos de la Persona ───────────────────────────
        Long personId,
        String name,
        String lastName,
        String address,
        String email,
        Long phoneNumber,
        Long typeIdentificationId,
        String typeIdentificationName,
        Long sexId,
        String sexName,

        // ── Datos del Usuario (si existe) ─────────────────
        Long userId,
        String userName,

        // ── Asignación de Rol actual (si tiene) ───────────
        Boolean hasActiveRole,
        String currentRoleName,
        Long currentRoleId,
        Long currentOutletId,
        String currentOutletName,
        Long assignmentId

) {}
