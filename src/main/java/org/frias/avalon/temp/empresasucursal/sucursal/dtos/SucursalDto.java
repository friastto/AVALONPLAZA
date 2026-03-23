package org.frias.avalon.temp.empresasucursal.sucursal.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.frias.avalon.domain.masterdata.dtos.response.MasterDataResponseDto;


@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class SucursalDto {
    private Long id;
    private String codigoSucursal;
    private String nombre;
    private String direccion;
    private String telefono;
    private boolean principal;
    private MasterDataResponseDto estado;
    //private EmpresaDto empresa;
}
