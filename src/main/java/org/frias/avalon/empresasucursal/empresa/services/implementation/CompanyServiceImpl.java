package org.frias.avalon.empresasucursal.empresa.services.implementation;

import jakarta.persistence.EntityExistsException;

import jakarta.persistence.EntityNotFoundException;
import org.frias.avalon.domain.inventory.Producto.modules.admincompany.services.interfaces.ProductoCompanyService;
import org.frias.avalon.domain.inventory.Producto.modules.adminsaas.dtos.ProductResponseDto;
import org.frias.avalon.domain.inventory.Producto.modules.adminsaas.entities.Product;
import org.frias.avalon.domain.inventory.Producto.modules.adminsaas.entities.ProductCompany;
import org.frias.avalon.domain.inventory.Producto.modules.adminsaas.mappers.ProductoMapperService;
import org.frias.avalon.domain.inventory.Producto.modules.adminsaas.services.interfaces.ProductoService;
import org.frias.avalon.empresasucursal.empresa.dtos.CompanyRequestNewDto;
import org.frias.avalon.empresasucursal.empresa.dtos.CompanyResponseDto;
import org.frias.avalon.empresasucursal.empresa.entities.Company;
import org.frias.avalon.empresasucursal.empresa.mappers.CompanyMapper;
import org.frias.avalon.empresasucursal.empresa.repositories.CompanyRepository;
import org.frias.avalon.empresasucursal.empresa.services.interfaces.CompanyService;

import org.frias.avalon.empresasucursal.sucursal.dtos.OutletRequestNewDto;
import org.frias.avalon.empresasucursal.sucursal.entities.Outlet;
import org.frias.avalon.empresasucursal.tenant.tenantcontex.TenantContext;
import org.frias.avalon.maestra.entities.MasterData;
import org.frias.avalon.maestra.services.interfaces.MasterDataService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CompanyServiceImpl implements CompanyService {


    private final CompanyRepository companyRepository;

    private final MasterDataService maestraRepository;

    private final CompanyMapper companyMapper;

    public CompanyServiceImpl(CompanyRepository companyRepository, MasterDataService maestraRepository, CompanyMapper companyMapper) {
        this.companyRepository = companyRepository;
        this.maestraRepository = maestraRepository;
        this.companyMapper = companyMapper;

    }

    @Transactional
    @Override
    public CompanyResponseDto save(CompanyRequestNewDto dto) {

        companyRepository.findByNit(dto.nit()).ifPresent(e -> {
            throw new EntityExistsException("El NIT digitado * " + dto.nit() + " * no se encuentra disponible");
        });

        MasterData estadoActivo = maestraRepository.searchShortName("ACT");

        Company company = new Company();

        company.setNit(dto.nit());
        company.setName(dto.name());
        company.setEmail(dto.email());
        company.setStatus(estadoActivo);

        final Company companySaved = companyRepository.saveAndFlush(company);

        System.out.println("codigo generado de la company : " +company.getId());
        // 2. Verificamos que el ID no sea nulo o cero antes de seguir
        if (companySaved.getId() == null || companySaved.getId() == 0) {
            throw new IllegalStateException("La empresa no generó un ID válido");
        }

           for(OutletRequestNewDto outletDto : dto.outlets()) {
               Outlet o = new Outlet();
               o.codeGenerator();
               o.setName(outletDto.name());
               o.setAddress(outletDto.address());
               o.setPhone(outletDto.phone());
               o.setMain(false);
               o.setStatus(estadoActivo);
               o.setLatitude(outletDto.latitude());
               o.setLongitude(outletDto.longitude());

               // 🔥 ESTE ES EL CAMPO QUE IMPORTA


               // solo para mantener la relación en memoria (NO DB)
               o.setCompany(companySaved);



               companySaved.addSucursal(o);
           };

        //se marca la priemra sucursal creada como la primera
        if (!companySaved.getOutlets().isEmpty()) {
            companySaved.getOutlets().get(0).setMain(true);
        }
        System.out.println("datos antes de la actualizacion : => "+companySaved.toString());
        companySaved.getOutlets().forEach(o -> {
            System.out.println(
                    "company.id=" + o.getCompany().getId() +
                            " | empresaId=" + o.getEmpresaId()
            );
        });
        try {

            return companyMapper.toDto(companyRepository.save(companySaved));

        }catch (Exception e) {
            e.getStackTrace();
           throw new IllegalArgumentException("no se puede guardar la empresa " + e.getMessage());
        }

    }

    @Override
    public Company findById(Long id) {

        return companyRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("No se encontro la empresa"));

    }

    @Override
    public CompanyResponseDto searchCompanyAndOutlets(Long id) {

        return companyMapper.toDto(searchCompany(id));
    }

    @Override
    public CompanyResponseDto searchCompanyAndOutlets() {
        return companyMapper.toDto(searchCompany());
    }



    public Company searchCompany(Long id) {
        return companyRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("No se encuentran registros de la compañia en la base de datos"));
    }

    @Override
    public Company searchCompany() {

        return searchCompany(TenantContext.getTenantId());
    }
}
