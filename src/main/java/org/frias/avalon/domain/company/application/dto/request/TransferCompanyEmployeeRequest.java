package org.frias.avalon.domain.company.application.dto.request;

import jakarta.validation.constraints.NotNull;

/**
 * DTO for transferring an employee to another store/outlet within the same company.
 */
public record TransferCompanyEmployeeRequest(
        @NotNull(message = "El ID de la tienda destino es obligatorio")
        Long targetOutletId
) {}
