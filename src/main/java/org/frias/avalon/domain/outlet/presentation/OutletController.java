package org.frias.avalon.domain.outlet.presentation;

import org.frias.avalon.core.exeptions.ApiResponse;
import org.frias.avalon.domain.outlet.application.dto.request.FindOutletRequestDto;
import org.frias.avalon.domain.outlet.application.dto.request.OutletCreateRequestDto;
import org.frias.avalon.domain.outlet.application.dto.request.OutletNearbyByRadiusRequestDto;
import org.frias.avalon.domain.outlet.application.dto.request.OutletSearchCriteria;
import org.frias.avalon.domain.outlet.application.dto.response.OutletDetailResponse;
import org.frias.avalon.domain.outlet.application.dto.response.OutletLightResponse;
import org.frias.avalon.domain.outlet.application.dto.response.OutletResponseDto;
import org.frias.avalon.domain.outlet.application.usecase.create.CreateOutletUseCase;
import org.frias.avalon.domain.outlet.application.usecase.find.FindAllOutletsUseCase;
import org.frias.avalon.domain.outlet.application.usecase.find.FindNearbyOutletsLightUseCase;
import org.frias.avalon.domain.outlet.application.usecase.find.FindOutletDetailByIdUseCase;
import org.frias.avalon.domain.outlet.application.usecase.find.FindOutletNearbyByRadiusUseCase;
import org.frias.avalon.domain.outlet.application.usecase.find.FindOutletUseCase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.frias.avalon.domain.outlet.application.dto.response.OutletDashboardResponse;
import org.frias.avalon.domain.outlet.application.dto.response.CashCutResponse;
import org.frias.avalon.domain.outlet.application.usecase.find.GetOutletDashboardUseCase;
import org.frias.avalon.domain.outlet.application.usecase.update.ExecuteCashCutUseCase;

import java.util.List;

@RestController
@RequestMapping("/avalon/outlet")
public class OutletController {

    private final CreateOutletUseCase createUseCase;
    private final FindOutletNearbyByRadiusUseCase findOutletNearbyByRadiusUseCase;
    private final FindOutletUseCase findOutletUseCase;
    private final FindNearbyOutletsLightUseCase findNearbyOutletsLightUseCase;
    private final FindOutletDetailByIdUseCase findOutletDetailByIdUseCase;
    private final FindAllOutletsUseCase findAllOutletsUseCase;
    private final GetOutletDashboardUseCase getOutletDashboardUseCase;
    private final ExecuteCashCutUseCase executeCashCutUseCase;

    public OutletController(CreateOutletUseCase createUseCase, FindOutletNearbyByRadiusUseCase findOutletNearbyByRadiusUseCase, FindOutletUseCase findOutletUseCase, FindNearbyOutletsLightUseCase findNearbyOutletsLightUseCase, FindOutletDetailByIdUseCase findOutletDetailByIdUseCase, FindAllOutletsUseCase findAllOutletsUseCase, GetOutletDashboardUseCase getOutletDashboardUseCase, ExecuteCashCutUseCase executeCashCutUseCase) {
        this.createUseCase = createUseCase;
        this.findOutletNearbyByRadiusUseCase = findOutletNearbyByRadiusUseCase;
        this.findOutletUseCase = findOutletUseCase;
        this.findNearbyOutletsLightUseCase = findNearbyOutletsLightUseCase;
        this.findOutletDetailByIdUseCase = findOutletDetailByIdUseCase;
        this.findAllOutletsUseCase = findAllOutletsUseCase;
        this.getOutletDashboardUseCase = getOutletDashboardUseCase;
        this.executeCashCutUseCase = executeCashCutUseCase;
    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<OutletResponseDto>> create(@RequestBody OutletCreateRequestDto data) {

        OutletResponseDto newOutlet;
        try {
            newOutlet = createUseCase.execute(data);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return ResponseEntity.status(201)
                .body(new ApiResponse<>(
                                201,
                                "se creo la tienda exitosamente",
                                newOutlet
                        )
                );

    }

    @GetMapping("/find")
    public ResponseEntity<ApiResponse<List<OutletResponseDto>>> find(
            @RequestParam(required = false) String nit,
            @RequestParam(required = false) String name
    ) {
        FindOutletRequestDto request = new FindOutletRequestDto(name, nit);
        List<OutletResponseDto> outlets = findOutletUseCase.execute(request);
        return ResponseEntity.status(200)
                .body(new ApiResponse<>(
                                outlets.isEmpty() ? 404 : 200,
                                outlets.isEmpty() ? "No se encontraron tiendas con los criterios proporcionados" : "Se encontraron tiendas",
                                outlets
                        )
                );
    }

    @PostMapping("/nearby/v1")
    public ResponseEntity<ApiResponse<List<OutletResponseDto>>> nearbyByRadius(@RequestBody OutletNearbyByRadiusRequestDto data) {

        List<OutletResponseDto> newOutlet;
        try {
            newOutlet = findOutletNearbyByRadiusUseCase.execute(data);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        return ResponseEntity.status(200)
                .body(new ApiResponse<>(
                                newOutlet.isEmpty() ? 404 : 200,
                                newOutlet.isEmpty() ? "no tiene tiendas cerca" : "se encontraron tiendas cercanas",
                                newOutlet
                        )
                );

    }

    @PostMapping("/nearby/light")
    public ResponseEntity<ApiResponse<List<OutletLightResponse>>> nearbyByRadiusLight(@RequestBody OutletNearbyByRadiusRequestDto data) {
        List<OutletLightResponse> outlets = findNearbyOutletsLightUseCase.execute(data);
        return ResponseEntity.status(200)
                .body(new ApiResponse<>(
                                outlets.isEmpty() ? 404 : 200,
                                outlets.isEmpty() ? "No tiene tiendas cerca" : "Se encontraron tiendas cercanas",
                                outlets
                        )
                );
    }

    @GetMapping("/{id}/detail")
    public ResponseEntity<ApiResponse<OutletDetailResponse>> getOutletDetail(@PathVariable Long id) {
        OutletDetailResponse outletDetail = findOutletDetailByIdUseCase.execute(id);
        return ResponseEntity.status(200)
                .body(new ApiResponse<>(
                                200,
                                "Detalle de la tienda obtenido exitosamente",
                                outletDetail
                        )
                );
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<org.springframework.data.web.PagedModel<OutletResponseDto>>> findAll(
            OutletSearchCriteria criteria,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        Page<OutletResponseDto> outlets = findAllOutletsUseCase.execute(criteria, pageable);
        return ResponseEntity.status(200)
                .body(new ApiResponse<>(
                                outlets.isEmpty() ? 404 : 200,
                                outlets.isEmpty() ? "No se encontraron tiendas registradas" : "Se encontraron tiendas registradas",
                                new org.springframework.data.web.PagedModel<>(outlets)
                        )
                );
    }

    @GetMapping("/{id}/employee-dashboard")
    public ResponseEntity<ApiResponse<OutletDashboardResponse>> getEmployeeDashboard(
            @PathVariable Long id,
            @RequestParam(defaultValue = "HOY") String filter
    ) {
        OutletDashboardResponse response = getOutletDashboardUseCase.execute(id, filter);
        return ResponseEntity.status(200)
                .body(new ApiResponse<>(200, "Dashboard de la tienda obtenido exitosamente", response));
    }

    @PostMapping("/{id}/cash-cut")
    public ResponseEntity<ApiResponse<CashCutResponse>> executeCashCut(@PathVariable Long id) {
        CashCutResponse response = executeCashCutUseCase.execute(id);
        return ResponseEntity.status(200)
                .body(new ApiResponse<>(200, "Arqueo de caja ejecutado exitosamente", response));
    }
}