package org.frias.avalon.domain.masterdata.infraestructure.persistence.adapter;

import org.frias.avalon.domain.masterdata.domain.model.MasterRoot;
import org.frias.avalon.domain.masterdata.domain.repository.MasterDataRepositoryPort;
import org.frias.avalon.domain.masterdata.infraestructure.mapper.MasterDataMapperService;
import org.frias.avalon.domain.masterdata.infraestructure.persistence.entity.MasterData;
import org.frias.avalon.domain.masterdata.infraestructure.persistence.repository.JpaMasterDataRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class MasterDataRepositoryAdapter implements MasterDataRepositoryPort {
    private final JpaMasterDataRepository jpa;
    private final MasterDataMapperService mapper;

    public MasterDataRepositoryAdapter(JpaMasterDataRepository jpa, MasterDataMapperService mapper) {
        this.jpa = jpa;
        this.mapper = mapper;
    }

    @Override
    public Optional<MasterRoot> findById(Long id) {

        return jpa.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<MasterRoot> findByCode(String code) {


        return jpa.findByShortName(code).map(mapper::toDomain);
    }

    @Override
    public Long getIdByCode(String code) {

        return jpa.findIdByShortName(code);
    }

    @Override
    public String getCodeById(Long id) {

        return jpa.findShortNameById(id);
    }

    @Override
    public MasterRoot save(MasterRoot masterData) {

        MasterData md = mapper.toEntity(masterData);

        MasterData mdSaved = jpa.save(md);

        return mapper.toDomain(mdSaved);
    }

    @Override
    public MasterRoot deleteById(Long id) {

        return null;
    }

    @Override
    public Optional<MasterRoot> findParentByChildrenId(Long chilldrenId) {

        return jpa.findParentByChildId(chilldrenId).map(mapper::toDomain);
    }

    @Override
    public Optional<MasterRoot> getActiveStatus() {

        return findByCode("ACT");

    }

    @Override
    public List<MasterRoot> findAll() {
        return jpa.findAll().stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<MasterRoot> saveAll(List<MasterRoot> mdList2) {

        List<MasterData> uaList = mdList2.stream().map(mapper::toEntity).toList();

        return uaList.stream().map(mapper::toDomain).toList();
    }

}
