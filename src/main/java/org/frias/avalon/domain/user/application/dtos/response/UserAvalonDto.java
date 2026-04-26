package org.frias.avalon.domain.user.application.dtos.response;

public record UserAvalonDto(
        Long id,

        String identificationId,

        String numberId,

        String userName,

        String rol,

        String fullName,

        String address,

        String sexId

) {
}
