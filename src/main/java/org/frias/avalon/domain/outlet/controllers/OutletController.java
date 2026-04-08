package org.frias.avalon.domain.outlet.controllers;


import org.frias.avalon.domain.outlet.dtos.request.OutletNewDto;
import org.frias.avalon.domain.outlet.dtos.response.OutletDto;
import org.frias.avalon.domain.outlet.dtos.request.OutletMap;
import org.frias.avalon.domain.outlet.entities.Outlet;
import org.frias.avalon.domain.outlet.services.interfaces.OutletService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/outlet")
public class OutletController {

private final OutletService outletService;

    public OutletController(OutletService outletService) {
        this.outletService = outletService;
    }



 @GetMapping("/all")
    public List<OutletDto> getAll() {
        return outletService.getAll();
 }

    @PostMapping("/add")
    public Outlet add(@RequestBody OutletNewDto outletDto) {
        return outletService.create(outletDto);
    }

    @PostMapping("/nearby")
    public List<OutletDto> getOutletsNerby(@RequestBody OutletMap outletDto) {

        System.out.println(outletDto.toString());
        return outletService.searchNearbyStores(outletDto);
    }


}
