package org.frias.avalon.domain.user.application.dtos.response;

public record ApplicantVerifiedResponseDto(
        Long userId,
        Long personId,
        String fullName,
        String email,
        String verificationToken
) {
}
