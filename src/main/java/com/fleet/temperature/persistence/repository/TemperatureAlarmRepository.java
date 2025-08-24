package com.fleet.temperature.persistence.repository;

import com.fleet.temperature.persistence.entity.TemperatureAlarm;
import com.fleet.temperature.persistence.enums.AlarmSeverity;
import com.fleet.temperature.persistence.enums.AlarmStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TemperatureAlarmRepository extends JpaRepository<TemperatureAlarm, Long> {
    
    List<TemperatureAlarm> findByAircraftIdOrderByTriggeredAtDesc(Long aircraftId);
    
    List<TemperatureAlarm> findByStatus(AlarmStatus status);
    
    List<TemperatureAlarm> findBySeverity(AlarmSeverity severity);
    
    List<TemperatureAlarm> findByAircraftIdAndStatusOrderByTriggeredAtDesc(
        Long aircraftId, AlarmStatus status);
    
    List<TemperatureAlarm> findByStatusAndTriggeredAtBetween(
        AlarmStatus status, LocalDateTime startTime, LocalDateTime endTime);
    
    @Query("SELECT ta FROM TemperatureAlarm ta WHERE ta.status = 'ACTIVE' " +
           "ORDER BY ta.triggeredAt DESC")
    List<TemperatureAlarm> findActiveAlarms();
    
    @Query("SELECT ta FROM TemperatureAlarm ta WHERE ta.aircraft.id = :aircraftId " +
           "AND ta.status = 'ACTIVE' ORDER BY ta.triggeredAt DESC")
    List<TemperatureAlarm> findActiveAlarmsByAircraft(@Param("aircraftId") Long aircraftId);
    
    @Query("SELECT COUNT(ta) FROM TemperatureAlarm ta WHERE ta.aircraft.id = :aircraftId " +
           "AND ta.status = :status")
    Long countAlarmsByAircraftAndStatus(
        @Param("aircraftId") Long aircraftId,
        @Param("status") AlarmStatus status);
    
    @Query("SELECT COUNT(ta) FROM TemperatureAlarm ta WHERE ta.status = 'ACTIVE' " +
           "AND ta.severity = :severity")
    Long countActiveAlarmsBySeverity(@Param("severity") AlarmSeverity severity);
}
