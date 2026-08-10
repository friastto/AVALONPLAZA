package org.frias.avalon.domain.masterdata.infraestructure.persistence.repository;

import org.frias.avalon.domain.masterdata.infraestructure.persistence.entity.MasterData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JpaMasterDataRepository extends JpaRepository<MasterData, Long> {

    boolean existsByParentId(Long parentId);

    Optional<MasterData> findByShortName(String shortName);

    @Query("""
            SELECT m.id FROM MasterData m WHERE m.shortName = :code
            """)
    Long findIdByShortName(String code);

    @Query("""
            SELECT m.shortName FROM MasterData m WHERE m.id = :id
            """)
    String findShortNameById(Long id);


    @Query("""
                select p from MasterData p
                where p.id = (
                    select m.parentId from MasterData m where m.id = :id
                )
            """)
    Optional<MasterData> findParentByChildId(@Param("id") Long childId);

    @Query(value = """
        WITH RECURSIVE tree AS (
            SELECT m.* FROM master_data m WHERE m.parent_id = (
                SELECT p.id FROM master_data p WHERE p.short_name = :parentCode
            )
            UNION ALL
            SELECT c.* FROM master_data c
            INNER JOIN tree t ON c.parent_id = t.id
        )
        SELECT * FROM tree
    """, nativeQuery = true)
    List<MasterData> findChildrenByParentCode(@Param("parentCode") String parentCode);
}