package org.frias.avalon.maestra.repositories;

import org.frias.avalon.maestra.entities.MasterData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MasterDataRepository extends JpaRepository<MasterData, Long> {


   @Query ("""
   select m from MasterData m
   where m.shortName = :shortName
   and m.statusId = (select m.id from MasterData m where m.shortName = "ACT")   
""")
   Optional< MasterData> findByShortNameAndStatusActive(String shortName);

   @Query ("""
   select m from MasterData m
   where m.id = :id
   and m.statusId = (select m.id from MasterData m where m.shortName = "ACT")   
""")
   Optional< MasterData> findByIdAndStatusActive(Long id);


}
