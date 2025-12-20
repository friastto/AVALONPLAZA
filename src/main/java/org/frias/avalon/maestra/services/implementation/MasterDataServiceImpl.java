package org.frias.avalon.maestra.services.implementation;

import org.frias.avalon.maestra.dtos.MasterDataRequestCreateDto;
import org.frias.avalon.maestra.dtos.MasterDataResponseDto;
import org.frias.avalon.maestra.entities.MasterData;
import org.frias.avalon.maestra.mappers.service.interfaces.MasterDataMapperService;
import org.frias.avalon.maestra.repositories.MasterDataRepository;
import org.frias.avalon.maestra.services.interfaces.MasterDataService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MasterDataServiceImpl implements MasterDataService {

    private final MasterDataRepository mdRepository;
    private final MasterDataMapperService masterDataMapperService;

    private final String statusActiveShortName = "ACT";

    public MasterDataServiceImpl(MasterDataRepository mdRepository, MasterDataMapperService masterDataMapperService) {
        this.mdRepository = mdRepository;
        this.masterDataMapperService = masterDataMapperService;
    }

    @Transactional
    @Override
    public List<MasterDataResponseDto> saveAll(List<MasterDataRequestCreateDto> masterDataRequestList) {


        masterDataRequestList.forEach(masterDataRequest -> {

            MasterData masterData = new MasterData();
            masterData.setShortName(masterDataRequest.shortName());
            masterData.setFullName(masterDataRequest.fullName());

            if(masterDataRequest.parentShortName() != null) {

               masterData.setParentId(mdRepository.findByShortName(masterDataRequest.parentShortName()).get().getId());
         }

            masterData.setStatusId(mdRepository.findByShortName(statusActiveShortName).get().getId());



            mdRepository.save(masterData);

        });

           return   mdRepository.findAll()
                   .stream()
                   .map(masterDataMapperService::toDto)
                   .collect(Collectors.toList());
    }
}
