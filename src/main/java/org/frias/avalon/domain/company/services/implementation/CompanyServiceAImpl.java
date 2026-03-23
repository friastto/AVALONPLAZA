package org.frias.avalon.domain.company.services.implementation;

import jakarta.persistence.EntityExistsException;
import org.frias.avalon.domain.company.A.CompanyRequestNewDto;
import org.frias.avalon.domain.company.dtos.UpdateCompanyDto;
import org.frias.avalon.domain.company.entities.Company;
import org.frias.avalon.domain.company.mappers.CompanyMapper;
import org.frias.avalon.domain.company.repositories.CompanyRepository;
import org.frias.avalon.domain.company.services.interfaces.CompanyServiceA;
import org.frias.avalon.domain.masterdata.entities.MasterData;
import org.frias.avalon.domain.masterdata.services.interfaces.MasterDataService;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
public class CompanyServiceAImpl implements CompanyServiceA {

    private final CompanyRepository companyRepository;
    private final MasterDataService masterDataService;
    private final CompanyMapper companyMapper;



    public CompanyServiceAImpl(CompanyRepository companyRepository, MasterDataService masterDataService, CompanyMapper companyMapper) {
        this.companyRepository = companyRepository;
        this.masterDataService = masterDataService;
        this.companyMapper = companyMapper;
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

    @Override
    public Company searchByNit(String nit) {

        return companyRepository.findByNit(nit)
                .orElseThrow( ( ) -> new EntityExistsException("no se encontraron empresas relacionadas con el nit * " + nit + " *"));
    }

    @Override
    public Company searchById(Long id) {

        return companyRepository.findById(id)
                .orElseThrow( ( ) -> new EntityExistsException("no se encontraron empresas relacionadas con el id * " + id + " *"));
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

    private boolean checkCompanyExistsByNit(String nit){

       return  companyRepository.findByNit(nit).isPresent();
    }




}
