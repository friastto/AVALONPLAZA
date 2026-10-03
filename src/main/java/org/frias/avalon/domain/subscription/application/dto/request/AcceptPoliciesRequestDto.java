package org.frias.avalon.domain.subscription.application.dto.request;

/**
 * Request payload when accepting legal terms and policies.
 */
public record AcceptPoliciesRequestDto(
        String policiesVersion
) {
    public AcceptPoliciesRequestDto {
        if (policiesVersion == null || policiesVersion.isBlank()) {
            policiesVersion = "v1.0";
        }
    }
}
