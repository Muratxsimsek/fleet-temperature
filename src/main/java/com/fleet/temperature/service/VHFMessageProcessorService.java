package com.fleet.temperature.service;

import com.fleet.temperature.dto.AircraftDto;
import com.fleet.temperature.dto.VHFMessageDto;
import com.fleet.temperature.dto.TemperatureReadingDto;
import com.fleet.temperature.dto.TemperatureAlarmDto;
import com.fleet.temperature.persistence.entity.Aircraft;
import com.fleet.temperature.persistence.entity.CabinTemperatureReading;
import com.fleet.temperature.persistence.enums.CabinZone;
import com.fleet.temperature.persistence.enums.TemperatureStatus;
import com.fleet.temperature.persistence.entity.TemperatureAlarm;
import com.fleet.temperature.persistence.enums.AircraftStatus;
import com.fleet.temperature.persistence.repository.AircraftRepository;
import com.fleet.temperature.persistence.repository.CabinTemperatureReadingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class VHFMessageProcessorService {
    
    private static final Map<String, AircraftInfo> AIRCRAFT_INFO = Map.ofEntries(
        Map.entry("TC-ABC", new AircraftInfo("Boeing", "737-800", 2020, "TC-ABC", "IST-AMS")),
        Map.entry("TC-DEF", new AircraftInfo("Airbus", "A320", 2021, "TC-DEF", "IST-CDG")),
        Map.entry("TC-GHI", new AircraftInfo("Boeing", "777-300ER", 2019, "TC-GHI", "IST-JFK")),
        Map.entry("TC-JKL", new AircraftInfo("Airbus", "A330-300", 2020, "TC-JKL", "IST-LHR")),
        Map.entry("TC-MNO", new AircraftInfo("Boeing", "787-9", 2021, "TC-MNO", "IST-DXB")),
        Map.entry("TC-PQR", new AircraftInfo("Airbus", "A350-900", 2022, "TC-PQR", "IST-FRA")),
        Map.entry("TC-STU", new AircraftInfo("Boeing", "737-900ER", 2021, "TC-STU", "IST-MAD")),
        Map.entry("TC-VWX", new AircraftInfo("Airbus", "A321neo", 2022, "TC-VWX", "IST-ARN")),
        Map.entry("TC-AA1", new AircraftInfo("Boeing", "737-800", 2020, "TC-AA1", "IST-OSL")),
        Map.entry("TC-BB2", new AircraftInfo("Airbus", "A320", 2021, "TC-BB2", "IST-NRT")),
        Map.entry("TC-CC3", new AircraftInfo("Boeing", "777-300ER", 2019, "TC-CC3", "IST-BKK")),
        Map.entry("TC-DD4", new AircraftInfo("Airbus", "A350-900", 2022, "TC-DD4", "IST-DEL")),
        Map.entry("TC-EE5", new AircraftInfo("Boeing", "787-9", 2021, "TC-EE5", "IST-BOM")),
        Map.entry("TC-FF6", new AircraftInfo("Airbus", "A330-300", 2020, "TC-FF6", "IST-PEK")),
        Map.entry("TC-GG7", new AircraftInfo("Boeing", "737-900ER", 2021, "TC-GG7", "IST-SYD")),
        Map.entry("TC-HH8", new AircraftInfo("Airbus", "A321neo", 2022, "TC-HH8", "IST-MEL")),
        Map.entry("TC-II9", new AircraftInfo("Boeing", "767-300ER", 2018, "TC-II9", "IST-AKL")),
        Map.entry("TC-JJ0", new AircraftInfo("Airbus", "A380-800", 2019, "TC-JJ0", "IST-CPT")),
        Map.entry("TC-KK1", new AircraftInfo("Boeing", "747-8", 2020, "TC-KK1", "IST-JNB")),
        Map.entry("TC-LL2", new AircraftInfo("Airbus", "A330-200", 2021, "TC-LL2", "IST-GRU")),
        Map.entry("TC-MM3", new AircraftInfo("Boeing", "737-700", 2020, "TC-MM3", "IST-EZE")),
        Map.entry("TC-NN4", new AircraftInfo("Airbus", "A320neo", 2022, "TC-NN4", "IST-MEX")),
        Map.entry("TC-OO5", new AircraftInfo("Boeing", "787-8", 2021, "TC-OO5", "IST-BOG")),
        Map.entry("TC-PP6", new AircraftInfo("Airbus", "A350-1000", 2022, "TC-PP6", "IST-LAX"))
    );
    
    private record AircraftInfo(String manufacturer, String model, int year, String registrationNumber, String route) {}
    
    private final AircraftRepository aircraftRepository;
    private final CabinTemperatureReadingRepository temperatureReadingRepository;
    private final TemperatureAlarmService alarmService;
    private final WebSocketService webSocketService;
    private final DashboardService dashboardService;
    
    private static final double WARNING_THRESHOLD_CELSIUS = 25.0;
    private static final double CRITICAL_THRESHOLD_CELSIUS = 28.0;
    
    @Async("asyncExecutor")
    @Transactional(timeout = 30)
    public void processVHFMessage(VHFMessageDto message) {
        processSingleMessage(message);
    }
    
    @Async("asyncExecutor")
    @Transactional(timeout = 60)
    public void processBatchMessages(List<VHFMessageDto> messages) {
        if (messages == null || messages.isEmpty()) {
            return;
        }
        
        log.info("Processing batch of {} messages", messages.size());
        
        List<CabinTemperatureReading> readingsToSave = new ArrayList<>();
        List<TemperatureAlarm> alarmsToSave = new ArrayList<>();
        
        for (VHFMessageDto message : messages) {
            try {
                CabinTemperatureReading reading = processSingleMessage(message);
                if (reading != null) {
                    readingsToSave.add(reading);
                    
                    if (reading.getStatus() == TemperatureStatus.WARNING ||
                        reading.getStatus() == TemperatureStatus.CRITICAL) {
                        TemperatureAlarm alarm = alarmService.createTemperatureAlarm(reading, reading.getAircraft());
                        if (alarm != null) {
                            alarmsToSave.add(alarm);
                        }
                    }
                }
            } catch (Exception e) {
                log.error("Error processing message {} in batch: {}", message.getMessageId(), e.getMessage());
            }
        }
        
        if (!readingsToSave.isEmpty()) {
            List<CabinTemperatureReading> savedReadings = temperatureReadingRepository.saveAll(readingsToSave);
            log.info("Batch saved {} temperature readings", savedReadings.size());
            
            savedReadings.forEach(reading -> {
                webSocketService.broadcastTemperatureUpdate(convertToDto(reading));
                webSocketService.sendAircraftTemperature(reading.getAircraft().getAircraftId(), convertToDto(reading));
            });
        }
        
        if (!alarmsToSave.isEmpty()) {
            List<TemperatureAlarm> savedAlarms = alarmService.saveAllAlarms(alarmsToSave);
            log.info("Batch saved {} temperature alarms", savedAlarms.size());
            
            savedAlarms.forEach(alarm -> {
                try {
                    TemperatureAlarmDto alarmDto = alarmService.createAlarmDto(alarm);
                    webSocketService.broadcastTemperatureAlarm(alarmDto);
                    log.debug("Temperature alarm broadcasted via WebSocket: {}", alarm.getId());
                } catch (Exception e) {
                    log.warn("Failed to broadcast temperature alarm via WebSocket: {}", alarm.getId(), e);
                }
            });
        }
        
        try {
            List<AircraftDto> fleetOverview = dashboardService.getFleetTemperatureOverview();
            if (!fleetOverview.isEmpty()) {
                webSocketService.broadcastDashboardSummary(fleetOverview.get(0));
                log.debug("Dashboard summary broadcasted after batch processing");
            }
        } catch (Exception e) {
            log.warn("Failed to broadcast dashboard summary after batch processing: {}", e.getMessage());
        }
    }
    
    private CabinTemperatureReading processSingleMessage(VHFMessageDto message) {
        try {
            log.info("Processing VHF message: {} for aircraft: {}", 
                    message.getMessageId(), message.getAircraftId());
            
            if (temperatureReadingRepository.findByMessageId(message.getMessageId()).isPresent()) {
                log.warn("Duplicate message ID detected: {}", message.getMessageId());
                return null;
            }
            
            Aircraft aircraft = getOrCreateAircraft(message.getAircraftId());
            if (aircraft == null) {
                log.error("Failed to get or create aircraft: {}", message.getAircraftId());
                return null;
            }
            
            CabinTemperatureReading reading = createTemperatureReading(message, aircraft);
            if (reading == null) {
                log.error("Failed to create temperature reading for message: {}", message.getMessageId());
                return null;
            }
            
            determineTemperatureStatus(reading);
            
            CabinTemperatureReading savedReading = temperatureReadingRepository.save(reading);
            log.info("Successfully saved CabinTemperatureReading message: {} for aircraft: {}",
                    savedReading.getMessageId(), savedReading.getAircraft().getAircraftId());

            checkAndCreateAlarms(savedReading, aircraft);
            
            webSocketService.broadcastTemperatureUpdate(convertToDto(savedReading));
            webSocketService.sendAircraftTemperature(aircraft.getAircraftId(), convertToDto(savedReading));
            
            try {
                List<AircraftDto> fleetOverview = dashboardService.getFleetTemperatureOverview();
                if (!fleetOverview.isEmpty()) {
                    webSocketService.broadcastDashboardSummary(fleetOverview.get(0));
                    log.debug("Dashboard summary broadcasted after temperature update for aircraft: {}", aircraft.getAircraftId());
                }
            } catch (Exception e) {
                log.warn("Failed to broadcast dashboard summary after temperature update: {}", e.getMessage());
            }
            
            log.info("Successfully processed VHF message: {} for aircraft: {}", 
                    message.getMessageId(), message.getAircraftId());
            
            return savedReading;
                    
        } catch (Exception e) {
            log.error("Error processing VHF message: {}", message.getMessageId(), e);
            return null;
        }
    }
    
    private Aircraft getOrCreateAircraft(String aircraftId) {
        Optional<Aircraft> existingAircraft = aircraftRepository.findByAircraftId(aircraftId);
        if (existingAircraft.isPresent()) {
            return existingAircraft.get();
        }
        
        AircraftInfo aircraftInfo = AIRCRAFT_INFO.get(aircraftId);
        if (aircraftInfo == null) {
            log.warn("Aircraft ID {} not found in AIRCRAFT_INFO, using default values", aircraftId);
            aircraftInfo = new AircraftInfo("Unknown", "Unknown", 2024, aircraftId, "Unknown");
        }
        
        Aircraft newAircraft = Aircraft.builder()
            .aircraftId(aircraftId)
            .manufacturer(aircraftInfo.manufacturer())
            .model(aircraftInfo.model())
            .year(aircraftInfo.year())
            .registrationNumber(aircraftInfo.registrationNumber())
            .status(AircraftStatus.ACTIVE)
            .build();
        
        try {
            return aircraftRepository.save(newAircraft);
        } catch (Exception e) {
            log.error("Failed to create new aircraft: {}", aircraftId, e);
            return null;
        }
    }
    
    private CabinTemperatureReading createTemperatureReading(VHFMessageDto message, Aircraft aircraft) {
        try {
            LocalDateTime timestamp = message.getTs().atZone(ZoneId.systemDefault()).toLocalDateTime();
            double tempCelsius = message.getPayload().getTemperatureC();
            double tempFahrenheit = celsiusToFahrenheit(tempCelsius);
            
            CabinZone cabinZone = CabinZone.valueOf(message.getPayload().getCabinZone());
            
            return CabinTemperatureReading.builder()
                .messageId(message.getMessageId())
                .aircraft(aircraft)
                .timestamp(timestamp)
                .cabinZone(cabinZone)
                .temperatureCelsius(tempCelsius)
                .temperatureFahrenheit(tempFahrenheit)
                .status(TemperatureStatus.NORMAL)
                .build();
                
        } catch (Exception e) {
            log.error("Failed to create temperature reading from message: {}", message.getMessageId(), e);
            return null;
        }
    }
    
    private void determineTemperatureStatus(CabinTemperatureReading reading) {
        double tempCelsius = reading.getTemperatureCelsius();
        
        if (tempCelsius >= CRITICAL_THRESHOLD_CELSIUS) {
            reading.setStatus(TemperatureStatus.CRITICAL);
        } else if (tempCelsius >= WARNING_THRESHOLD_CELSIUS) {
            reading.setStatus(TemperatureStatus.WARNING);
        } else {
            reading.setStatus(TemperatureStatus.NORMAL);
        }
    }
    
    private void checkAndCreateAlarms(CabinTemperatureReading reading, Aircraft aircraft) {
        if (reading.getStatus() == TemperatureStatus.WARNING || 
            reading.getStatus() == TemperatureStatus.CRITICAL) {
            
            alarmService.createTemperatureAlarm(reading, aircraft);
        }
    }
    
    private double celsiusToFahrenheit(double celsius) {
        return (celsius * 9/5) + 32;
    }
    
    private TemperatureReadingDto convertToDto(CabinTemperatureReading reading) {
        AircraftInfo aircraftInfo = AIRCRAFT_INFO.get(reading.getAircraft().getAircraftId());
        
        return TemperatureReadingDto.builder()
                .id(reading.getId())
                .aircraftId(reading.getAircraft().getAircraftId())
                .cabinZone(reading.getCabinZone().toString())
                .temperatureCelsius(reading.getTemperatureCelsius())
                .temperatureFahrenheit(reading.getTemperatureFahrenheit())
                .status(reading.getStatus().toString())
                .route(aircraftInfo != null ? aircraftInfo.route() : "Unknown")
                .timestamp(reading.getTimestamp())
                .build();
    }
}
