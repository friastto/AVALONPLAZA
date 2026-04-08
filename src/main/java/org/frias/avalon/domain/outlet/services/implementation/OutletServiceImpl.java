package org.frias.avalon.domain.outlet.services.implementation;


import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.frias.avalon.core.jwt.util.JwtUtils;
import org.frias.avalon.core.tenant.config.TenantAware;
import org.frias.avalon.core.tenant.tenantcontex.TenantContext;
import org.frias.avalon.domain.company.entities.Company;
import org.frias.avalon.domain.company.facade.BaseTenantService;
import org.frias.avalon.domain.masterdata.entities.MasterData;
import org.frias.avalon.domain.masterdata.services.interfaces.MasterDataService;
import org.frias.avalon.domain.outlet.dtos.request.OutletMap;
import org.frias.avalon.domain.outlet.dtos.request.OutletNewDto;
import org.frias.avalon.domain.outlet.dtos.response.OutletDto;
import org.frias.avalon.domain.outlet.dtos.response.OutletWithCatalogProductResponse;
import org.frias.avalon.domain.outlet.entities.Outlet;
import org.frias.avalon.domain.outlet.mappers.OutletMapper;
import org.frias.avalon.domain.outlet.repositories.OutletRepository;
import org.frias.avalon.domain.outlet.services.interfaces.OutletService;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@TenantAware
@Service
public class OutletServiceImpl
        extends BaseTenantService
        implements OutletService {


    private final OutletRepository outletRepository;
    private final OutletMapper outletMapper;

    private final MasterDataService masterDataService;
    private final ApplicationContext applicationContext;
    private final JwtUtils jwtUtils;


    @PersistenceContext
    private EntityManager entityManager;

    public OutletServiceImpl(OutletRepository outletRepository, OutletMapper outletMapper, MasterDataService masterDataService, ApplicationContext applicationContext, JwtUtils jwtUtils) {


        this.outletRepository = outletRepository;
        this.outletMapper = outletMapper;

        this.masterDataService = masterDataService;
        this.applicationContext = applicationContext;
        this.jwtUtils = jwtUtils;


    }

    @Transactional
    @Override
    public Outlet create(OutletNewDto outletDto) {
        Long companyIdToAssign;

        // REGLA DE ORO: ¿Quién intenta crear?
        if (isMasterStaff()) {
            // Si eres TÚ (Master), la empresa DEBE venir en el DTO (porque tú creas para otros)
            if (outletDto.companyId() == null) {
                throw new IllegalArgumentException("Como Master, debes especificar a qué empresa pertenece la sucursal.");
            }
            companyIdToAssign = outletDto.companyId();
        } else {
            // Si es un Gerente de Company, usamos SU propio ID del token (Seguridad Total)
            companyIdToAssign = getValidatedCompanyId();

            // Validación extra: Un Gerente no puede crear sucursales para otra empresa
            if (outletDto.companyId() != null && !outletDto.companyId().equals(companyIdToAssign)) {
                throw new SecurityException("Violacion de seguridad * No puedes crear sucursales para una empresa diferente.");
            }
        }

        outletRepository.findByName(outletDto.name()).ifPresent(o -> {
            throw new EntityExistsException("el name de la sucursal no disponible :" + o);
        });




        MasterData estadoActivo = masterDataService.getStatusActive();

        Outlet o = new Outlet();

        o.codeGenerator();
        o.setName(outletDto.name());
        o.setAddress(outletDto.address());
        o.setPhone(outletDto.phone());
        o.setMain(false);
        o.setStatus(estadoActivo);
        o.setLatitude(outletDto.latitude());
        o.setLongitude(outletDto.longitude());
        o.setCompany(new Company(companyIdToAssign));

        return outletRepository.save(o);
    }

    @Override
    public List<OutletDto> getAll() {



        List<Outlet> outlets = outletRepository.findAll();


        // 2. Mapeas a DTO (esto mantiene tu Clean Architecture separando la Entidad del Response)
        return outlets.stream()
                .map(outletMapper::toDto)
                .collect(Collectors.toList());

    }

    @Override
    public Outlet searchById(Long id) {

        return outletRepository.findById(id)

                .orElseThrow(() -> new EntityExistsException("no se encuentro la sucursal en la base d ed atos "));
    }

    @Override
    public List<OutletDto> searchNearbyStores(OutletMap outletMap) {


        List<Outlet> outlets = outletRepository.searchNearbyStores(
                outletMap.query(),
                outletMap.lat(),
                outletMap.lng(),
                outletMap.radius()
        );

        if (outlets.isEmpty()) {
            return List.of(); // nunca retornar null

        }
        return outlets.stream()
                .map(outletMapper::toDto)
                .toList();
    }

    @Override
    public Boolean existsByIdAndCompanyId(Long idOutlet, Long idCompany) {

        // Si cualquiera es nulo, no hay relación posible
        if (idOutlet == null || idCompany == null) return false;

        return outletRepository.existsByIdAndCompanyId(idOutlet, idCompany);
    }



    public void validarAccesoSucursal(Long idOutletDestino) {
        Long idCompany = getValidatedCompanyId(); // Viene del padre

        // Usas el repo directamente aquí
        boolean esMia = outletRepository.existsByIdAndCompanyId(idOutletDestino, idCompany);

        if (!esMia) {
            throw new SecurityException("La sucursal no pertenece a su empresa.");
        }
    }
}
