package org.frias.avalon.domain.masterdata.infraestructure.persistence.adapter;

import org.frias.avalon.domain.masterdata.domain.model.MasterRoot;
import org.frias.avalon.domain.masterdata.domain.repository.MasterDataRepositoryPort;
import org.frias.avalon.domain.masterdata.infraestructure.persistence.entity.MasterData;
import org.frias.avalon.domain.masterdata.infraestructure.persistence.repository.JpaMasterDataRepository;
import org.frias.avalon.domain.masterdata.infraestructure.mapper.MasterDataMapperService;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
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
        return Optional.empty();
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
    public Optional<MasterRoot> findParentByIClilldrenId(Long chilldrenId) {

        return jpa.findParentByChildId(chilldrenId).map(mapper::toDomain);
    }







}
