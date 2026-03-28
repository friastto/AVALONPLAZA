package org.frias.avalon.domain.masterdata.repositories;

import org.frias.avalon.domain.masterdata.entities.MasterData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MasterDataRepository extends JpaRepository<MasterData, Long> {


    @Query("""
               select m from MasterData m
               where m.shortName = :shortName
               and m.statusId = (select s.id from MasterData s where s.shortName = 'ACT')   
            """)
    Optional<MasterData> findByShortNameAndStatusActive(@Param("shortName") String shortName);

    @Query("""
               select m from MasterData m
               where m.id = :id
               and m.statusId = (select s.id from MasterData s where s.shortName = 'ACT')
            """)
    Optional<MasterData> findByIdAndStatusActive(Long id);


    @Query("""
               select m from MasterData m
               join MasterData parent on m.parentId = parent.id
               where parent.shortName = :shortName
                 and m.statusId = (select md.id from MasterData md where md.shortName = 'ACT')
            """)
    List<MasterData> findAllChildrenByParentShortNameAndActive(@Param("shortName") String shortName);

    @Query("""
               select m from MasterData m
               join MasterData parent on m.parentId = parent.id
               where parent.id = :id
                 and m.statusId = (select md.id from MasterData md where md.shortName = 'ACT')
            """)
    List<MasterData> findAllChildrenByParentIdAndActive(@Param("id") Long id);


    @Query("""
            select m from MasterData m
            where m.statusId = (select md.id from MasterData md where md.shortName = 'ACT')
            """)
    List<MasterData> findAllActive();


}
