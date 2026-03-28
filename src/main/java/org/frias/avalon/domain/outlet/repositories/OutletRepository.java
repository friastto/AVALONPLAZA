package org.frias.avalon.domain.outlet.repositories;



import org.frias.avalon.domain.outlet.entities.Outlet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

//@TenantAware
@Repository
public interface OutletRepository extends JpaRepository<Outlet, Long> {


    Optional<Outlet> findByName(String name);

    @Query("""
        select o
        from Outlet o 
        where o.empresaId = :empresaId\s
        order by o.name ASC
       """)
    List<Outlet> findAllOuletsByEmpresa(@Param("empresaId") Long empresaId);

    @Query("""
        select o from Outlet o where(
            6371 * acos(
                cos(radians(:lat)) *
                cos(radians(o.latitude)) *
                cos(radians(o.longitude) - radians(:lng)) +
                sin(radians(:lat)) *
                sin(radians(o.latitude))
            )
        ) <= :radius
        AND LOWER(o.name) LIKE LOWER(CONCAT('%', :query, '%'))
    """)
    List<Outlet> searchNearbyStores(
            @Param("query") String query,
            @Param("lat") double lat,
            @Param("lng") double lng,
            @Param("radius") double radius
    );

    @Query("SELECT count(o) > 0 FROM Outlet o WHERE o.id = :idOutlet AND o.company.id= :idCompany")
    Boolean existsByIdAndCompanyId(@Param("idOutlet") Long idOutlet, @Param("idCompany") Long idCompany);


}
