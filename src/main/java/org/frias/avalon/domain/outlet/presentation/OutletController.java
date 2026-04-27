package org.frias.avalon.domain.outlet.presentation;

import org.frias.avalon.core.exeptions.ApiResponse;
import org.frias.avalon.domain.outlet.application.dto.request.OutletCreateRequestDto;
import org.frias.avalon.domain.outlet.application.dto.request.OutletNearbyByRadiusRequestDto;
import org.frias.avalon.domain.outlet.application.dto.response.OutletResponseDto;
import org.frias.avalon.domain.outlet.application.usecase.create.CreateOutletUseCase;
import org.frias.avalon.domain.outlet.application.usecase.find.FindOutletNearbyByRadiusUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/avalon/outlet")
public class OutletController {

    private final CreateOutletUseCase createUseCase;
    private final FindOutletNearbyByRadiusUseCase findOutletNearbyByRadiusUseCase;

    public OutletController(CreateOutletUseCase createUseCase, FindOutletNearbyByRadiusUseCase findOutletNearbyByRadiusUseCase) {
        this.createUseCase = createUseCase;
        this.findOutletNearbyByRadiusUseCase = findOutletNearbyByRadiusUseCase;
    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<OutletResponseDto>> create(@RequestBody OutletCreateRequestDto data){

        OutletResponseDto newOutlet;
try {
    newOutlet = createUseCase.execute(data);
} catch (Exception e) {
    e.printStackTrace();
    throw new RuntimeException(e);
}
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(
                        201,
                        "se creo la tienda exitosamente",
                        newOutlet
                        )
                );

    } @PostMapping("/nearby/v1")
    public ResponseEntity<ApiResponse<List<OutletResponseDto>>> nearbyByRadius(@RequestBody OutletNearbyByRadiusRequestDto data){

        List<OutletResponseDto> newOutlet;
        try {
            newOutlet = findOutletNearbyByRadiusUseCase.execute(data);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(
                        newOutlet.isEmpty()?404:200,
                                newOutlet.isEmpty()?"no tiene tiendas cerca":"se encontraron tiendas cercanas",
                                newOutlet
                        )
                );

    }
}
