package com.pgrental.backend.repository;

import com.pgrental.backend.entity.Pg;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface PgRepository extends JpaRepository<Pg, String> {

    List<Pg> findByOwnerId(String ownerId);
    
    @Query(value = "SELECT p.*, " +
           "ST_Distance(p.location_geom, ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)::geography) / 1000.0 AS distance " +
           "FROM pgs p " +
           "WHERE ST_DWithin(p.location_geom, ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)::geography, :radius * 1000) " +
           "ORDER BY distance ASC",
           countQuery = "SELECT count(*) FROM pgs p " +
           "WHERE ST_DWithin(p.location_geom, ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)::geography, :radius * 1000)",
           nativeQuery = true)
    Page<Pg> findNearby(@Param("lat") Double lat, @Param("lng") Double lng, @Param("radius") Double radius, Pageable pageable);
}
