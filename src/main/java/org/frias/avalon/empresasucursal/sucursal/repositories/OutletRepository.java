package org.frias.avalon.empresasucursal.sucursal.repositories;



import org.frias.avalon.empresasucursal.sucursal.entities.Outlet;
import org.frias.avalon.empresasucursal.tenant.config.TenantAware;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

//@TenantAware
@Repository
public interface OutletRepository extends JpaRepository<Outlet, Long> {

    List<Outlet> findByCompany(Long empresaId);
    Optional<Outlet> findByName(String name);

    @Query("""
        select o from Outlet o where o.empresaId = :empresaId
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

}
