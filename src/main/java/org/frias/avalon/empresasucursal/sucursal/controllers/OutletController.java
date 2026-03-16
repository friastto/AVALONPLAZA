package org.frias.avalon.empresasucursal.sucursal.controllers;


import org.frias.avalon.Producto.modules.adminsaas.dtos.ProductResponseDto;
import org.frias.avalon.empresasucursal.sucursal.dtos.OutletRequestNewDto;
import org.frias.avalon.empresasucursal.sucursal.dtos.OutletResponseDto;
import org.frias.avalon.empresasucursal.sucursal.entities.Outlet;
import org.frias.avalon.empresasucursal.sucursal.services.interfaces.ServiceSucursal;
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




}
