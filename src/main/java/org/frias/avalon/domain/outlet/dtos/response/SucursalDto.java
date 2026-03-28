package org.frias.avalon.domain.outlet.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.frias.avalon.domain.masterdata.dtos.response.MasterDataResponseDto;


public record  SucursalDto (
    Long id,
    String codigoSucursal,
    String nombre,
    String direccion,
    String telefono,
    boolean principal,
    MasterDataResponseDto estado
    //private EmpresaDto empresa;
    ){}
