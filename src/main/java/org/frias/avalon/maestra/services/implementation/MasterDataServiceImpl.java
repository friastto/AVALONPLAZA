package org.frias.avalon.maestra.services.implementation;

import jakarta.persistence.EntityNotFoundException;
import org.frias.avalon.maestra.dtos.MasterDataRequestCreateDto;
import org.frias.avalon.maestra.dtos.MasterDataResponseDto;
import org.frias.avalon.maestra.entities.MasterData;
import org.frias.avalon.maestra.mappers.service.interfaces.MasterDataMapperService;
import org.frias.avalon.maestra.repositories.MasterDataRepository;
import org.frias.avalon.maestra.services.interfaces.MasterDataProductService;
import org.frias.avalon.maestra.services.interfaces.MasterDataSalesService;
import org.frias.avalon.maestra.services.interfaces.MasterDataService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MasterDataServiceImpl implements MasterDataService,
        MasterDataProductService, MasterDataSalesService {

    private final MasterDataRepository mdRepository;
    private final MasterDataMapperService masterDataMapperService;

    private final String statusActiveShortName = "ACT";
    private final MasterDataRepository masterDataRepository;

    public MasterDataServiceImpl(MasterDataRepository mdRepository, MasterDataMapperService masterDataMapperService, MasterDataRepository masterDataRepository) {
        this.mdRepository = mdRepository;
        this.masterDataMapperService = masterDataMapperService;
        this.masterDataRepository = masterDataRepository;
    }

    @Transactional
    @Override
    public List<MasterDataResponseDto> saveAll(List<MasterDataRequestCreateDto> masterDataRequestList) {

        MasterData statusActive = mdRepository.findByShortNameAndStatusActive(statusActiveShortName)
                .orElseThrow(() -> new RuntimeException("Error Crítico: El estado ACTIVO no existe en MasterData"));

List<MasterData>  savedEntities = new ArrayList<>();
        for (MasterDataRequestCreateDto request : masterDataRequestList) {

            MasterData masterData = new MasterData();

            masterData.setShortName(request.shortName());
            masterData.setFullName(request.fullName());
            masterData.setStatusId(statusActive.getId());

            if (request.parentShortName() != null) {
                MasterData parent = mdRepository.findByShortNameAndStatusActive(request.parentShortName())
                        .orElseThrow(() -> new RuntimeException("No se encontró el padre: " + request.parentShortName()));

                masterData.setParentId(parent.getId());
            }

            mdRepository.save(masterData);
            // 3. Guardar y limpiar para que esté disponible para el siguiente hijo en el bucle
            savedEntities.add(mdRepository.save(masterData));

            mdRepository.flush(); // Fuerza a que el ID esté disponible inmediatamente

        };

           return   savedEntities.stream()
                .map(masterDataMapperService::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public MasterDataResponseDto findByNameShortDto(String nameShort) {

        return masterDataMapperService.toDto(mdRepository.findByShortNameAndStatusActive(nameShort)
                .orElseThrow(() -> new EntityNotFoundException("el tipo de componente {*" +nameShort +"*} no disponible ") ));
    }


    @Override
    public MasterData findClientByNameShort(String nameShort) {

        return mdRepository.findByShortNameAndStatusActive(nameShort)
                .orElseThrow(() -> new EntityNotFoundException("el tipo de componente {*" +nameShort +"*} no disponible ") );
    }

    @Override
    public MasterData findById(Long id) {

       return mdRepository.findByIdAndStatusActive(id)
                .orElseThrow(() -> new EntityNotFoundException("el tipo de componente no disponible ") );

    }



    @Override
    public MasterData searchShortName(String shortName) {

        return masterDataRepository.findByShortNameAndStatusActive(shortName)
                .orElseThrow(() -> new EntityNotFoundException("no se puede establecer el tipo"));

    }

}
