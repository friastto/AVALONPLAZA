package org.frias.avalon.domain.person.application.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record VerificationResponseDto(
        Boolean personExists,
        Boolean userExists,
        String nameHint
) {
}