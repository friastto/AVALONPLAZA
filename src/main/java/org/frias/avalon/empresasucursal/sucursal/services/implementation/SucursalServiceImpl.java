package org.frias.avalon.empresasucursal.sucursal.services.implementation;


import jakarta.persistence.EntityExistsException;
import org.frias.avalon.empresasucursal.empresa.entities.Company;
import org.frias.avalon.empresasucursal.empresa.services.interfaces.CompanyService;
import org.frias.avalon.empresasucursal.sucursal.dtos.OutletRequestNewDto;
import org.frias.avalon.empresasucursal.sucursal.dtos.OutletResponseDto;
import org.frias.avalon.empresasucursal.sucursal.entities.Outlet;
import org.frias.avalon.empresasucursal.sucursal.mappers.OutletMapper;
import org.frias.avalon.empresasucursal.sucursal.repositories.OutletRepository;
import org.frias.avalon.empresasucursal.sucursal.services.interfaces.ServiceSucursal;
import org.frias.avalon.empresasucursal.tenant.config.TenantAware;
import org.frias.avalon.empresasucursal.tenant.tenantcontex.TenantContext;
import org.frias.avalon.jwt.util.JwtUtils;
import org.frias.avalon.maestra.entities.MasterData;
import org.frias.avalon.maestra.repositories.MasterDataRepository;
import org.frias.avalon.maestra.services.interfaces.MasterDataService;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@TenantAware
@Service
public class SucursalServiceImpl implements ServiceSucursal {


    private final OutletRepository outletRepository;
    private final OutletMapper outletMapper;

    private final MasterDataService masterDataService;
    private final ApplicationContext applicationContext;
    private final JwtUtils jwtUtils;
    private final CompanyService companyService;

    public SucursalServiceImpl(OutletRepository outletRepository, OutletMapper outletMapper, MasterDataService masterDataService, ApplicationContext applicationContext, JwtUtils jwtUtils, CompanyService companyService) {
        this.outletRepository = outletRepository;
        this.outletMapper = outletMapper;

        this.masterDataService = masterDataService;
        this.applicationContext = applicationContext;
        this.jwtUtils = jwtUtils;
        this.companyService = companyService;
    }

    @Transactional
    @Override
    public OutletResponseDto save(OutletRequestNewDto outletDto) {

        outletRepository.findByName(outletDto.name()).ifPresent(o -> {
            throw new EntityExistsException("el nombre de la sucursal no disponible :"+ o);
        });

        Long company_id = TenantContext.getTenantId();

        Company company = new Company();
        company.setId(company_id);

        MasterData estadoActivo = masterDataService.searchShortName("ACT");

        Outlet o = new Outlet();

        o.codeGenerator();
        o.setName(outletDto.name());
        o.setAddress(outletDto.address());
        o.setPhone(outletDto.phone());
        o.setMain(false);
        o.setStatus(estadoActivo);
        o.setLatitude(outletDto.latitude());
        o.setLongitude(outletDto.longitude());
        o.setCompany(company);



        return outletMapper.toDto(outletRepository.save(o));
    }

    @Override
    public List<OutletResponseDto> getAll() {
        System.out.println("ID EN SERVICE: " + TenantContext.getTenantId());
        List<Outlet> outlets = outletRepository.findAll();


        // 2. Mapeas a DTO (esto mantiene tu Clean Architecture separando la Entidad del Response)
        return outlets.stream()
                .map(outletMapper::toDto)
                .collect(Collectors.toList());

    }


}
