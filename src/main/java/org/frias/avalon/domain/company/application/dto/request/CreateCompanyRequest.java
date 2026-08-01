package org.frias.avalon.domain.company.application.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO for creating a new Company.
 */
public record CreateCompanyRequest(

        @NotBlank(message = "NIT is required")
        @Size(max = 20, message = "NIT must not exceed 20 characters")
        String nit,

        @NotBlank(message = "Company name is required")
        @Size(max = 255, message = "Name must not exceed 255 characters")
        String name,

        @Email(message = "Email must be valid")
        String email

) {
}
