package org.frias.avalon.domain.masterdata.services.implementation;

import jakarta.persistence.EntityNotFoundException;
import org.frias.avalon.domain.masterdata.dtos.request.MasterDataNewDto;
import org.frias.avalon.domain.masterdata.entities.MasterData;
import org.frias.avalon.domain.masterdata.repositories.MasterDataRepository;
import org.frias.avalon.domain.masterdata.services.interfaces.MasterDataProductService;
import org.frias.avalon.domain.masterdata.services.interfaces.MasterDataSalesService;
import org.frias.avalon.domain.masterdata.services.interfaces.MasterDataService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class MasterDataServiceImpl implements MasterDataService,
        MasterDataProductService, MasterDataSalesService
{


    private final MasterDataRepository masterDataRepository;

    public MasterDataServiceImpl( MasterDataRepository masterDataRepository) {


        this.masterDataRepository = masterDataRepository;
    }


    @Override
    public MasterData create(MasterDataNewDto request) {

        MasterData masterData = new MasterData();

        masterData.setShortName(request.shortName());
        masterData.setFullName(request.fullName());

        masterData.setStatusId(getStatusActive().getId());

        if (request.parentShortName() != null) {
            MasterData parent = masterDataRepository.findByShortNameAndStatusActive(request.parentShortName())
                    .orElseThrow(() -> new RuntimeException("No se encontró el padre: " + request.parentShortName()));

            masterData.setParentId(parent.getId());
        }

        masterDataRepository.save(masterData);
        // 3. Guardar y limpiar para que esté disponible para el siguiente hijo en el bucle

       return masterDataRepository.saveAndFlush(masterData);
    }

    @Transactional
    @Override
    public List<MasterData> createAll(List<MasterDataNewDto> masterDataRequestList) {

        List<MasterData>  savedEntities = new ArrayList<>();
        for (MasterDataNewDto request : masterDataRequestList) {

            savedEntities.add(create(request));
        };

        return   savedEntities;
    }

    @Override
    public MasterData searchById(Long id) {

       return masterDataRepository.findByIdAndStatusActive(id)
                .orElseThrow(() -> new EntityNotFoundException("no se encontraron resultados en el arbol de Tipos de Datos") );

    }

    @Override
    public MasterData searchByShortName(String shortName) {

        return masterDataRepository.findByShortNameAndStatusActive(shortName.toUpperCase())
                .orElseThrow(() -> new EntityNotFoundException("no se encontro el tipo de dato buscado en eel arbol de tipos"));

    }

    @Override
    public MasterData searchByNameShortAndStatusActive(String nameShort) {

        return masterDataRepository.findByShortNameAndStatusActive(nameShort)
                .orElseThrow(() -> new EntityNotFoundException("el Topo de componente = { * " +nameShort +" * } no se encontro en el arbol de tipos") );
    }

    @Override
    public MasterData getRootBranch(Long id, String rootShortName) {


        MasterData current = searchById(id);

        if (current == null) return null;

        // CASO BASE: Si el padre es 'ROL' (la raíz de todo),
        // entonces 'current' es la rama principal (ej: ADMIN o GERENTE o DIREC)
        MasterData parent = (current.getParentId() != null)
                ? searchById(current.getParentId())
                : null;

        if (parent != null && parent.getShortName().equalsIgnoreCase(rootShortName)) {
            return current;
        }

        // Si no tiene padre, él mismo es la raíz
        if (current.getParentId() == null) {
            return current;
        }

        // Seguimos escalando
        return getRootBranch(current.getParentId(), rootShortName);
    }


    @Override
    public boolean isFromHierarchy(Long id, String branchName) {
        MasterData result = getRootBranch(id, branchName);
        return result != null && result.getShortName().equals(branchName);
    }

    @Override
    public List<MasterData> getAllSonWithStatusActiveByParentNameShort(String parentShortName) {

        List<MasterData> masterDataLis = masterDataRepository.findAllChildrenByParentShortNameAndActive(parentShortName);

        if(masterDataLis.isEmpty())
            throw new EntityNotFoundException("no se encontraron registros en la gerarquia del tipo seleccionado");


        return masterDataLis;

    }

    @Override
    public List<MasterData> getAllSonWithStatusActiveByParentId(Long idParent) {

        List<MasterData> masterDataLis = masterDataRepository.findAllChildrenByParentIdAndActive(idParent);

        if(masterDataLis.isEmpty())
            throw new EntityNotFoundException("no se encontraron registros en la gerarquia del tipo seleccionado");


        return masterDataLis;
    }

    @Override
    public List<MasterData> getAllWithStatusActive() {
        List<MasterData> masterDataLis = masterDataRepository.findAllActive();

        if(masterDataLis.isEmpty())
            throw new EntityNotFoundException("no se encontraron registros activos en el arbol de tipos");


        return masterDataLis;
    }

    @Override
    public MasterData getStatusActive() {
        return  searchByShortName("ACT");
    }
}
