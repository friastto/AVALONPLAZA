package org.frias.avalon.temp.empresasucursal.sucursal.controllers;


import org.frias.avalon.temp.empresasucursal.sucursal.dtos.OutletRequestNewDto;
import org.frias.avalon.temp.empresasucursal.sucursal.dtos.OutletResponseDto;
import org.frias.avalon.temp.empresasucursal.sucursal.dtos.OutletsRequestMap;
import org.frias.avalon.temp.empresasucursal.sucursal.services.interfaces.ServiceSucursal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/outlet")
public class OutletController {

private final ServiceSucursal serviceSucursal;

    public OutletController(ServiceSucursal serviceSucursal) {
        this.serviceSucursal = serviceSucursal;
    }



 @GetMapping("/all")
    public List<OutletResponseDto> getAll() {
        return serviceSucursal.getAll();
 }

    @PostMapping("/add")
    public OutletResponseDto add(@RequestBody OutletRequestNewDto outletDto) {
        return serviceSucursal.save(outletDto);
    }


    @PostMapping("/nearby")
    public List<OutletResponseDto> getOutletsNerby(@RequestBody OutletsRequestMap outletDto) {
        System.out.println(outletDto.toString());


        return serviceSucursal.searchNearbyStores(outletDto);
    }


}
