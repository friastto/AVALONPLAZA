package org.frias.avalon.domain.outlet.infraestructure.repository;

import org.frias.avalon.domain.outlet.infraestructure.entities.Outlet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JpaOutletRepository extends JpaRepository<Outlet, Long> {
    @Query(value = """
    SELECT *,
           ST_Distance(
               location::geography,
               ST_SetSRID(ST_MakePoint(:lon, :lat), 4326)::geography
           ) AS distance
    FROM outlet
    WHERE ST_DWithin(
        location::geography,
        ST_SetSRID(ST_MakePoint(:lon, :lat), 4326)::geography,
        :radius
    )
    ORDER BY distance
""", nativeQuery = true)
    List<Outlet> findNearByOrderByDistance(
            double lat,
            double lon,
            double radius
    );
}
