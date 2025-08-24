package com.fleet.temperature.persistence.repository;

import com.fleet.temperature.persistence.entity.CabinTemperatureReading;
import com.fleet.temperature.persistence.enums.CabinZone;
import com.fleet.temperature.persistence.enums.TemperatureStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CabinTemperatureReadingRepository extends JpaRepository<CabinTemperatureReading, Long> {
    
    Optional<CabinTemperatureReading> findByMessageId(String messageId);
    
    List<CabinTemperatureReading> findByAircraftIdOrderByTimestampDesc(Long aircraftId);
    
    List<CabinTemperatureReading> findByAircraftIdAndCabinZoneOrderByTimestampDesc(
        Long aircraftId, CabinZone cabinZone);
    
    List<CabinTemperatureReading> findByAircraftIdAndTimestampBetweenOrderByTimestampDesc(
        Long aircraftId, LocalDateTime startTime, LocalDateTime endTime);
    
    List<CabinTemperatureReading> findByStatus(TemperatureStatus status);
    
    List<CabinTemperatureReading> findByTemperatureCelsiusGreaterThan(Double temperature);
    
    List<CabinTemperatureReading> findByTemperatureCelsiusLessThan(Double temperature);
    
    @Query("SELECT ctr FROM CabinTemperatureReading ctr WHERE ctr.aircraft.id = :aircraftId " +
           "AND ctr.cabinZone = :cabinZone AND ctr.timestamp >= :startTime " +
           "ORDER BY ctr.timestamp DESC")
    List<CabinTemperatureReading> findReadingsByAircraftAndZoneAndTimeRange(
        @Param("aircraftId") Long aircraftId,
        @Param("cabinZone") CabinZone cabinZone,
        @Param("startTime") LocalDateTime startTime);
    
    @Query("SELECT AVG(ctr.temperatureCelsius) FROM CabinTemperatureReading ctr " +
           "WHERE ctr.aircraft.id = :aircraftId AND ctr.cabinZone = :cabinZone " +
           "AND ctr.timestamp >= :startTime")
    Double getAverageTemperatureByAircraftAndZone(
        @Param("aircraftId") Long aircraftId,
        @Param("cabinZone") CabinZone cabinZone,
        @Param("startTime") LocalDateTime startTime);
    
    @Query("SELECT MAX(ctr.temperatureCelsius) FROM CabinTemperatureReading ctr " +
           "WHERE ctr.aircraft.id = :aircraftId AND ctr.cabinZone = :cabinZone " +
           "AND ctr.timestamp >= :startTime")
    Double getMaxTemperatureByAircraftAndZone(
        @Param("aircraftId") Long aircraftId,
        @Param("cabinZone") CabinZone cabinZone,
        @Param("startTime") LocalDateTime startTime);
    
    @Query("SELECT MIN(ctr.temperatureCelsius) FROM CabinTemperatureReading ctr " +
           "WHERE ctr.aircraft.id = :aircraftId AND ctr.cabinZone = :cabinZone " +
           "AND ctr.timestamp >= :startTime")
    Double getMinTemperatureByAircraftAndZone(
        @Param("aircraftId") Long aircraftId,
        @Param("cabinZone") CabinZone cabinZone,
        @Param("startTime") LocalDateTime startTime);
    
    @Query("SELECT ctr FROM CabinTemperatureReading ctr WHERE ctr.aircraft.id = :aircraftId " +
           "AND ctr.cabinZone = :cabinZone ORDER BY ctr.timestamp DESC LIMIT 1")
    Optional<CabinTemperatureReading> findLatestReadingByAircraftAndZone(
        @Param("aircraftId") Long aircraftId,
        @Param("cabinZone") CabinZone cabinZone);
    
    @Query("SELECT COUNT(ctr) FROM CabinTemperatureReading ctr " +
           "WHERE ctr.aircraft.id = :aircraftId AND ctr.status = :status")
    Long countReadingsByAircraftAndStatus(
        @Param("aircraftId") Long aircraftId,
        @Param("status") TemperatureStatus status);
    
    @Query("SELECT ctr FROM CabinTemperatureReading ctr WHERE ctr.aircraft.id = :aircraftId " +
           "AND ctr.timestamp BETWEEN :startTime AND :endTime ORDER BY ctr.timestamp DESC")
    List<CabinTemperatureReading> findReadingsByAircraftAndTimeRange(
        @Param("aircraftId") Long aircraftId,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime);

    @Query("SELECT ctr FROM CabinTemperatureReading ctr WHERE ctr.timestamp BETWEEN :startTime AND :endTime " +
           "ORDER BY ctr.timestamp DESC")
    List<CabinTemperatureReading> findReadingsByTimeRange(
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime);
}
