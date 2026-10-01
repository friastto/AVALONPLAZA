package org.frias.avalon.domain.company.application.dto.request;

/**
 * DTO for transferring an employee to another store/outlet within the same company,
 * or assigning floating multi-outlet scope.
 */
public record TransferCompanyEmployeeRequest(
        Long targetOutletId,
        Boolean isFloating,
        String transferReason
) {
    public TransferCompanyEmployeeRequest(Long targetOutletId) {
        this(targetOutletId, false, null);
    }
}
