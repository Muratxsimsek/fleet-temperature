package com.fleet.temperature.service;

import com.fleet.temperature.dto.TemperatureAlarmDto;
import com.fleet.temperature.persistence.entity.Aircraft;
import com.fleet.temperature.persistence.entity.CabinTemperatureReading;
import com.fleet.temperature.persistence.entity.TemperatureAlarm;
import com.fleet.temperature.persistence.enums.AlarmSeverity;
import com.fleet.temperature.persistence.enums.AlarmStatus;
import com.fleet.temperature.persistence.repository.TemperatureAlarmRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TemperatureAlarmService {
    
    private final TemperatureAlarmRepository alarmRepository;
    private final WebSocketService webSocketService;
    
    public TemperatureAlarm createTemperatureAlarm(CabinTemperatureReading reading, Aircraft aircraft) {
        try {
            AlarmSeverity severity = determineAlarmSeverity(reading);
            String thresholdUnit = "CELSIUS";
            double thresholdTemperature = reading.getTemperatureCelsius();
            
            TemperatureAlarm alarm = TemperatureAlarm.builder()
                .aircraft(aircraft)
                .reading(reading)
                .severity(severity)
                .thresholdTemperature(thresholdTemperature)
                .thresholdUnit(thresholdUnit)
                .triggeredAt(LocalDateTime.now())
                .resolvedAt(null)
                .status(AlarmStatus.ACTIVE)
                .description(generateAlarmDescription(reading, severity))
                .build();
            
            TemperatureAlarm savedAlarm = alarmRepository.save(alarm);
            log.info("Created temperature alarm: {} for aircraft: {} zone: {}", 
                    savedAlarm.getId(), aircraft.getAircraftId(), reading.getCabinZone());
            
            try {
                TemperatureAlarmDto alarmDto = createAlarmDto(savedAlarm);
                webSocketService.broadcastTemperatureAlarm(alarmDto);
                log.debug("Temperature alarm broadcasted via WebSocket for aircraft: {}", aircraft.getAircraftId());
            } catch (Exception e) {
                log.warn("Failed to broadcast temperature alarm via WebSocket: {}", e.getMessage());
            }
            
            return savedAlarm;
            
        } catch (Exception e) {
            log.error("Failed to create temperature alarm for reading: {}", reading.getId(), e);
            return null;
        }
    }
    
    private AlarmSeverity determineAlarmSeverity(CabinTemperatureReading reading) {
        double temp = reading.getTemperatureCelsius();
        
        if (temp < 18.0) {
            return AlarmSeverity.MEDIUM; 
        } else if (temp >= 18.0 && temp <= 22.0) {
            return AlarmSeverity.LOW; 
        } else if (temp > 22.0 && temp <= 26.0) {
            return AlarmSeverity.LOW; 
        } else if (temp > 26.0 && temp <= 30.0) {
            return AlarmSeverity.HIGH; 
        } else { 
            return AlarmSeverity.CRITICAL; 
        }
    }
    
    private String generateAlarmDescription(CabinTemperatureReading reading, AlarmSeverity severity) {
        double temp = reading.getTemperatureCelsius();
        String zone = reading.getCabinZone().name();
        
        if (temp < 18.0) {
            return String.format("Temperature in %s zone is too cold: %.1f°C - WARNING", zone, temp);
        } else if (temp >= 18.0 && temp <= 22.0) {
            return String.format("Temperature in %s zone is cool: %.1f°C - NORMAL", zone, temp);
        } else if (temp > 22.0 && temp <= 26.0) {
            return String.format("Temperature in %s zone is warm: %.1f°C - NORMAL", zone, temp);
        } else if (temp > 26.0 && temp <= 30.0) {
            return String.format("Temperature in %s zone is hot: %.1f°C - WARNING", zone, temp);
        } else { 
            return String.format("Temperature in %s zone is critical: %.1f°C - CRITICAL", zone, temp);
        }
    }
    
    public TemperatureAlarmDto createAlarmDto(TemperatureAlarm alarm) {
        double temp = alarm.getThresholdTemperature();
        String status = determineStatusFromTemperature(temp);
        String notes = generateStatusNotes(temp);
        
        return TemperatureAlarmDto.builder()
                .id(alarm.getId())
                .aircraftId(alarm.getAircraft().getAircraftId())
                .cabinZone(alarm.getReading().getCabinZone().name())
                .status(status)
                .temperatureCelsius(temp)
                .notes(notes)
                .timestamp(alarm.getTriggeredAt())
                .build();
    }
    
    private String determineStatusFromTemperature(double temperature) {
        if (temperature < 18.0) {
            return "WARNING";
        } else if (temperature >= 18.0 && temperature <= 26.0) {
            return "NORMAL";
        } else if (temperature > 26.0 && temperature <= 30.0) {
            return "WARNING";
        } else { 
            return "CRITICAL";
        }
    }
    
    private String generateStatusNotes(double temperature) {
        if (temperature < 18.0) {
            return "Temperature below cold threshold (< 18°C)";
        } else if (temperature >= 18.0 && temperature <= 22.0) {
            return "Temperature in cool range (18-22°C)";
        } else if (temperature > 22.0 && temperature <= 26.0) {
            return "Temperature in warm range (22-26°C)";
        } else if (temperature > 26.0 && temperature <= 30.0) {
            return "Temperature above hot threshold (> 26°C)";
        } else { 
            return "Temperature exceeds critical limit (> 30°C)";
        }
    }
    
    public List<TemperatureAlarm> getActiveAlarms() {
        return alarmRepository.findActiveAlarms();
    }
    
    public List<TemperatureAlarmDto> getActiveAlarmsAsDto() {
        List<TemperatureAlarm> alarms = getActiveAlarms();
        return alarms.stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }
    
    public List<TemperatureAlarm> getActiveAlarmsByAircraft(Long aircraftId) {
        return alarmRepository.findActiveAlarmsByAircraft(aircraftId);
    }
    
    public TemperatureAlarm acknowledgeAlarm(Long alarmId) {
        try {
            TemperatureAlarm alarm = alarmRepository.findById(alarmId)
                .orElseThrow(() -> new IllegalArgumentException("Alarm not found: " + alarmId));
            
            alarm.setStatus(AlarmStatus.ACKNOWLEDGED);
            alarm.setResolvedAt(LocalDateTime.now());
            
            TemperatureAlarm updatedAlarm = alarmRepository.save(alarm);
            log.info("Alarm acknowledged: {}", alarmId);
            
            return updatedAlarm;
            
        } catch (Exception e) {
            log.error("Failed to acknowledge alarm: {}", alarmId, e);
            return null;
        }
    }
    
    public TemperatureAlarm resolveAlarm(Long alarmId) {
        try {
            TemperatureAlarm alarm = alarmRepository.findById(alarmId)
                .orElseThrow(() -> new IllegalArgumentException("Alarm not found: " + alarmId));
            
            alarm.setStatus(AlarmStatus.RESOLVED);
            alarm.setResolvedAt(LocalDateTime.now());
            
            TemperatureAlarm updatedAlarm = alarmRepository.save(alarm);
            log.info("Alarm resolved: {}", alarmId);
            
            return updatedAlarm;
            
        } catch (Exception e) {
            log.error("Failed to resolve alarm: {}", alarmId, e);
            return null;
        }
    }
    
    public Long getActiveAlarmCount() {
        return alarmRepository.countAlarmsByAircraftAndStatus(null, AlarmStatus.ACTIVE);
    }
    
    public Long getActiveAlarmCountByAircraft(Long aircraftId) {
        return alarmRepository.countAlarmsByAircraftAndStatus(aircraftId, AlarmStatus.ACTIVE);
    }
    
    public TemperatureAlarm getAlarmById(Long id) {
        try {
            return alarmRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Alarm not found: " + id));
        } catch (Exception e) {
            log.error("Failed to get alarm by ID: {}", id, e);
            throw new RuntimeException("Failed to retrieve alarm: " + e.getMessage());
        }
    }
    
    public void deleteAlarm(Long id) {
        try {
            TemperatureAlarm alarm = getAlarmById(id);
            alarmRepository.delete(alarm);
            log.info("Alarm deleted: {}", id);
        } catch (Exception e) {
            log.error("Failed to delete alarm: {}", id, e);
            throw new RuntimeException("Failed to delete alarm: " + e.getMessage());
        }
    }
    
    private TemperatureAlarmDto convertToDto(TemperatureAlarm alarm) {
        return TemperatureAlarmDto.builder()
                .id(alarm.getId())
                .aircraftId(alarm.getAircraft().getAircraftId())
                .readingId(alarm.getReading().getId())
                .severity(alarm.getSeverity().name())
                .thresholdTemperature(alarm.getThresholdTemperature())
                .thresholdUnit(alarm.getThresholdUnit())
                .triggeredAt(alarm.getTriggeredAt())
                .resolvedAt(alarm.getResolvedAt())
                .status(alarm.getStatus().name())
                .description(alarm.getDescription())
                .createdAt(alarm.getCreatedAt())
                .build();
    }
    
    public List<TemperatureAlarm> saveAllAlarms(List<TemperatureAlarm> alarms) {
        return alarmRepository.saveAll(alarms);
    }
}
