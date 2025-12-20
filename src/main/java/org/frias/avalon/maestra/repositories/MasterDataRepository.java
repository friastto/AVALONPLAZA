package org.frias.avalon.maestra.repositories;

import org.frias.avalon.maestra.entities.MasterData;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MasterDataRepository extends JpaRepository<MasterData, Long> {

   Optional< MasterData> findByShortName(String shortName);
}
