package org.frias.avalon.domain.subscription.presentation;

import org.frias.avalon.core.exeptions.ApiResponse;
import org.frias.avalon.core.tenant.TenantContext;
import org.frias.avalon.domain.subscription.application.dto.request.AcceptPoliciesRequestDto;
import org.frias.avalon.domain.subscription.application.dto.response.CompanyOutletsSubscriptionSummaryDto;
import org.frias.avalon.domain.subscription.application.dto.response.OutletCheckoutSessionResponseDto;
import org.frias.avalon.domain.subscription.application.usecase.AcceptCompanyPoliciesUseCase;
import org.frias.avalon.domain.subscription.application.usecase.CreateOutletCheckoutSessionUseCase;
import org.frias.avalon.domain.subscription.application.usecase.FindOutletSubscriptionUseCase;
import org.frias.avalon.domain.subscription.application.usecase.FindOutletsSubscriptionStatusByCompanyUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for store subscriptions, Wompi checkout sessions, and policy acceptance.
 */
@RestController
@RequestMapping({"/avalon/subscriptions", "/api/v1/subscriptions"})
public class SubscriptionController {

    private final FindOutletsSubscriptionStatusByCompanyUseCase findSummaryUseCase;
    private final FindOutletSubscriptionUseCase findOutletSubscriptionUseCase;
    private final CreateOutletCheckoutSessionUseCase createCheckoutSessionUseCase;
    private final AcceptCompanyPoliciesUseCase acceptPoliciesUseCase;

    public SubscriptionController(
            FindOutletsSubscriptionStatusByCompanyUseCase findSummaryUseCase,
            FindOutletSubscriptionUseCase findOutletSubscriptionUseCase,
            CreateOutletCheckoutSessionUseCase createCheckoutSessionUseCase,
            AcceptCompanyPoliciesUseCase acceptPoliciesUseCase
    ) {
        this.findSummaryUseCase = findSummaryUseCase;
        this.findOutletSubscriptionUseCase = findOutletSubscriptionUseCase;
        this.createCheckoutSessionUseCase = createCheckoutSessionUseCase;
        this.acceptPoliciesUseCase = acceptPoliciesUseCase;
    }

    @GetMapping("/outlets/{outletId}")
    public ResponseEntity<ApiResponse<org.frias.avalon.domain.subscription.application.dto.response.OutletSubscriptionResponseDto>> getOutletSubscription(
            @PathVariable Long outletId
    ) {
        var sub = findOutletSubscriptionUseCase.execute(outletId);
        return ResponseEntity.ok(new ApiResponse<>(200, "Suscripcion de tienda obtenida con exito", sub));
    }

    @GetMapping({"/companies/{companyId}", "/companies/{companyId}/summary"})
    public ResponseEntity<ApiResponse<CompanyOutletsSubscriptionSummaryDto>> getCompanySummary(
            @PathVariable Long companyId
    ) {
        CompanyOutletsSubscriptionSummaryDto summary = findSummaryUseCase.execute(companyId);
        return ResponseEntity.ok(new ApiResponse<>(200, "Resumen de suscripciones obtenido con exito", summary));
    }

    @GetMapping("/companies/my/summary")
    public ResponseEntity<ApiResponse<CompanyOutletsSubscriptionSummaryDto>> getMyCompanySummary() {
        Long companyId = TenantContext.getTenantId();
        if (companyId == null) {
            throw new IllegalArgumentException("No se encontro el identificador de compania en la sesion");
        }
        CompanyOutletsSubscriptionSummaryDto summary = findSummaryUseCase.execute(companyId);
        return ResponseEntity.ok(new ApiResponse<>(200, "Resumen de suscripciones de tu compania", summary));
    }

    @PostMapping("/outlets/{outletId}/checkout-session")
    public ResponseEntity<ApiResponse<OutletCheckoutSessionResponseDto>> createCheckoutSession(
            @PathVariable Long outletId
    ) {
        OutletCheckoutSessionResponseDto session = createCheckoutSessionUseCase.execute(outletId);
        return ResponseEntity.ok(new ApiResponse<>(200, "Sesion de pago Wompi generada exitosamente", session));
    }

    @PostMapping({"/companies/{companyId}/accept-policies", "/companies/accept-policies"})
    public ResponseEntity<ApiResponse<String>> acceptPolicies(
            @PathVariable(required = false) Long companyId,
            @RequestBody(required = false) AcceptPoliciesRequestDto request
    ) {
        Long resolvedCompanyId = companyId != null ? companyId : TenantContext.getTenantId();
        if (resolvedCompanyId == null) {
            throw new IllegalArgumentException("Se requiere el identificador de la compania para aceptar las politicas");
        }
        acceptPoliciesUseCase.execute(resolvedCompanyId, request);
        return ResponseEntity.ok(new ApiResponse<>(200, "Politicas y terminos comerciales aceptados exitosamente", "OK"));
    }
}
