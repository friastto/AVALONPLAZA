package org.frias.avalon.domain.product.application.dto.request;

import jakarta.validation.constraints.NotNull;

/**
 * DTO for changing the status of a product.
 *
 * @param newStatusId The ID of the new status.
 */
public record ChangeStatusRequest(
    @NotNull(message = "The new status ID cannot be null.")
    Long newStatusId
) {}
