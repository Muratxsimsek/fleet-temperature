package com.fleet.temperature.persistence.repository;

import com.fleet.temperature.persistence.entity.Aircraft;
import com.fleet.temperature.persistence.enums.AircraftStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AircraftRepository extends JpaRepository<Aircraft, Long> {
    
    Optional<Aircraft> findByAircraftId(String aircraftId);
    
    Optional<Aircraft> findByRegistrationNumber(String registrationNumber);
    
    List<Aircraft> findByStatus(AircraftStatus status);
    
    List<Aircraft> findByManufacturerAndModel(String manufacturer, String model);
    
    @Query("SELECT a FROM Aircraft a WHERE a.status = 'ACTIVE'")
    List<Aircraft> findActiveAircraft();
    
    @Query("SELECT a FROM Aircraft a WHERE a.year >= :minYear")
    List<Aircraft> findAircraftByMinYear(@Param("minYear") Integer minYear);
    
    boolean existsByAircraftId(String aircraftId);
    
    boolean existsByRegistrationNumber(String registrationNumber);
}
