package org.frias.avalon.domain.user.application.dtos.response;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ForgotPasswordResponseDto(
        String maskedEmail
) {
}