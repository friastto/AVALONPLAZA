package org.frias.avalon.domain.user.application.dtos.response;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApplicantLookupResponseDto(
        Boolean userExists,
        String maskedEmail
) {
}
