package org.frias.avalon.domain.company.application.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO for updating an employee's employment status in a company or outlet.
 * Uses semantic status code (e.g., "ACT", "INA", "SUSP").
 */
public record UpdateCompanyEmployeeStatusRequest(
        @NotBlank(message = "El codigo de estado es obligatorio")
        String statusCode,
        Long outletId
) {}
