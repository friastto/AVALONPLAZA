package org.frias.avalon.domain.company.services.implementation;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import org.frias.avalon.core.tenant.tenantcontex.TenantContext;
import org.frias.avalon.domain.company.dtos.CompanyRequestNewDto;
import org.frias.avalon.domain.company.dtos.CompanyResponseDto;
import org.frias.avalon.domain.company.dtos.UpdateCompanyDto;
import org.frias.avalon.domain.company.entities.Company;
import org.frias.avalon.domain.company.facade.BaseTenantService;
import org.frias.avalon.domain.company.mappers.CompanyMapper;
import org.frias.avalon.domain.company.repositories.CompanyRepository;
import org.frias.avalon.domain.company.services.interfaces.CompanyService;
import org.frias.avalon.domain.masterdata.entities.MasterData;
import org.frias.avalon.domain.masterdata.services.interfaces.MasterDataService;
import org.frias.avalon.domain.outlet.dtos.request.OutletNewDto;
import org.frias.avalon.domain.outlet.services.interfaces.OutletService;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CompanyServiceImpl extends BaseTenantService
        implements CompanyService {


    private final CompanyRepository companyRepository;

    private final MasterDataService masterDataService;

    private final CompanyMapper companyMapper;

    private final OutletService outletService;


    public CompanyServiceImpl(CompanyRepository companyRepository, MasterDataService masterDataService, CompanyMapper companyMapper, OutletService outletService) {
        this.companyRepository = companyRepository;
        this.masterDataService = masterDataService;
        this.companyMapper = companyMapper;
        this.outletService = outletService;
    }
    private boolean checkCompanyExistsByNit(String nit){

        return  companyRepository.findByNit(nit).isPresent();
    }
    @Override
    public Company create(CompanyRequestNewDto newCompanyData) {

        if(checkCompanyExistsByNit(newCompanyData.nit()))
            throw new EntityExistsException("El NIT digitado * " + newCompanyData.nit() + " * no se encuentra disponible");

        MasterData statusActive = masterDataService.getStatusActive();

        Company company = new Company();

        company.setNit(newCompanyData.nit());
        company.setName(newCompanyData.name());
        company.setEmail(newCompanyData.email());
        company.setStatus(statusActive);

        final Company companySaved = companyRepository.saveAndFlush(company);

        try {
            return companyRepository.save(companySaved);
        } catch (DataIntegrityViolationException e) {

            throw new RuntimeException("El NIT ya existe o los datos son inválidos", e);
        } catch (DataAccessException e) {

            throw new RuntimeException("No se pudo guardar la empresa", e);
        }
    }

    @Transactional
    @Override
    public CompanyResponseDto createCompanyWhitOutlets(CompanyRequestNewDto dto) {

        if (!isMasterStaff())
            throw new SecurityException("no tiene los permisos necesarios para crear la empresa." +
                    "\n comuniquese con un administrador de AVALON...");

        companyRepository.findByNit(dto.nit()).ifPresent(e -> {
            throw new EntityExistsException("El NIT digitado * " + dto.nit() + " * no se encuentra disponible");
        });

        MasterData estadoActivo = masterDataService.searchByShortName("ACT");

        Company company = new Company();

        company.setNit(dto.nit());
        company.setName(dto.name());
        company.setEmail(dto.email());
        company.setStatus(estadoActivo);

        final Company companySaved = companyRepository.saveAndFlush(company);

        System.out.println("codigo generado de la company : " + company.getId());
        // 2. Verificamos que el ID no sea nulo o cero antes de seguir
        if (companySaved.getId() == null || companySaved.getId() == 0) {
            throw new IllegalStateException("La empresa no generó un ID válido");
        }

        for (OutletNewDto outletDto : dto.outlets()) {

             OutletNewDto oN = new OutletNewDto(
                                                outletDto.name(),
                                                outletDto.address(),
                                                outletDto.phone(),
                                                outletDto.latitude(),
                                                outletDto.longitude(),
                                                companySaved.getId()
                                            );

           companySaved.addSucursal(outletService.create(oN));
        }


        //se marca la priemra sucursal creada como la primera
        if (!companySaved.getOutlets().isEmpty()) {
            companySaved.getOutlets().get(0).setMain(true);
        }
        System.out.println("datos antes de la actualizacion : => " + companySaved);
        companySaved.getOutlets().forEach(o -> {
            System.out.println(
                    "company.id=" + o.getCompany().getId() +
                            " | empresaId=" + o.getEmpresaId()
            );
        });
        try {

            return companyMapper.toDto(companyRepository.save(companySaved));

        } catch (Exception e) {
            e.getStackTrace();
            throw new IllegalArgumentException("no se puede guardar la empresa " + e.getMessage());
        }



    }

    @Override
    public Company searchById(Long id) {

        return companyRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No se encontro la empresa"));

    }

    @Override
    public Company searchByNit(String nit) {

        return companyRepository.findByNit(nit)
                .orElseThrow( ( ) -> new EntityExistsException("no se encontraron empresas relacionadas con el nit * " + nit + " *"));
    }

    @Override
    public Company update(UpdateCompanyDto request) {

        Company companyOld = searchById(request.id());

        companyOld.setName(request.name());

        companyOld.setNit(request.nit());

        companyOld.setAddress(request.address().trim());

        companyOld.setEmail(request.email());
        try{

            return companyRepository.save(companyOld);
        } catch (DataIntegrityViolationException e) {

            throw new RuntimeException("El NIT esta duplicado o los datos son inválidos", e);
        } catch (DataAccessException e) {

            throw new RuntimeException("No se pudo actualizar los datos de la empresa", e);
        }
    }

    @Override
    public Company updateStatus(Long idCompany, Long idStatus) {

        Company company = searchById(idCompany);
        MasterData newStatus = masterDataService.searchById(idStatus);

        company.setStatus(newStatus);

        try{

            return companyRepository.save(company);
        } catch (DataIntegrityViolationException e) {

            throw new RuntimeException("No se pudo actualizar el estado de la empresa, * los datos son inválidos", e);
        } catch (DataAccessException e) {

            throw new RuntimeException("No se pudo actualizar el estado de la empresa", e);
        }
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
