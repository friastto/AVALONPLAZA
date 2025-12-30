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


        masterDataRequestList.forEach(masterDataRequest -> {


            MasterData masterData = new MasterData();
            masterData.setShortName(masterDataRequest.shortName());
            masterData.setFullName(masterDataRequest.fullName());

            if(masterDataRequest.parentShortName() != null) {

               masterData.setParentId(mdRepository.findByShortNameAndStatusActive(masterDataRequest.parentShortName()).get().getId());
         }

            masterData.setStatusId(mdRepository.findByShortNameAndStatusActive(statusActiveShortName).get().getId());



            mdRepository.save(masterData);

        });

           return   mdRepository.findAll()
                   .stream()
                   .map(masterDataMapperService::toDto)
                   .collect(Collectors.toList());
    }

    @Override
    public MasterDataResponseDto findByNameShort(String nameShort) {

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
                .orElseThrow(() -> new EntityNotFoundException("no se puede establecer el estado de lav enta como finalizado"));

    }

}
