package com.fleet.temperature.service;

import com.fleet.temperature.dto.AircraftDto;
import com.fleet.temperature.dto.AircraftDto.CabinZoneTemperatureDto;
import com.fleet.temperature.dto.AircraftDto.TemperatureStatusDto;
import com.fleet.temperature.dto.TemperatureReadingDto;
import com.fleet.temperature.persistence.entity.Aircraft;
import com.fleet.temperature.persistence.entity.CabinTemperatureReading;
import com.fleet.temperature.persistence.enums.CabinZone;
import com.fleet.temperature.persistence.enums.TemperatureStatus;
import com.fleet.temperature.persistence.repository.AircraftRepository;
import com.fleet.temperature.persistence.repository.CabinTemperatureReadingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class DashboardService {
    
    private final AircraftRepository aircraftRepository;
    private final CabinTemperatureReadingRepository temperatureReadingRepository;
    private final TemperatureAlarmService alarmService;
    private final WebSocketService webSocketService;
    
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
    
    private static class AircraftInfo {
        private final String manufacturer;
        private final String model;
        private final int year;
        private final String registrationNumber;
        private final String route;
        
        public AircraftInfo(String manufacturer, String model, int year, String registrationNumber, String route) {
            this.manufacturer = manufacturer;
            this.model = model;
            this.year = year;
            this.registrationNumber = registrationNumber;
            this.route = route;
        }
        
        public String getManufacturer() { return manufacturer; }
        public String getModel() { return model; }
        public int getYear() { return year; }
        public String getRegistrationNumber() { return registrationNumber; }
        public String getRoute() { return route; }
    }
    
    public List<AircraftDto> getFleetTemperatureOverview() {
        try {
            List<Aircraft> activeAircraft = aircraftRepository.findActiveAircraft();
            
            List<AircraftDto> fleetOverview = activeAircraft.stream()
                .map(this::buildAircraftDto)
                .collect(Collectors.toList());
            
            try {
                if (!fleetOverview.isEmpty()) {
                    webSocketService.broadcastDashboardSummary(fleetOverview.get(0));
                    log.debug("Dashboard summary broadcasted via WebSocket");
                }
            } catch (Exception e) {
                log.warn("Failed to broadcast dashboard summary via WebSocket: {}", e.getMessage());
            }
            
            return fleetOverview;
                
        } catch (Exception e) {
            log.error("Error getting fleet temperature overview", e);
            return new ArrayList<>();
        }
    }
    
    public List<AircraftDto> getAllAircraftTemperatures() {
        return getFleetTemperatureOverview();
    }
    
    public AircraftDto getAircraftTemperatureDetails(Long aircraftId) {
        try {
            Optional<Aircraft> aircraftOpt = aircraftRepository.findById(aircraftId);
            if (aircraftOpt.isEmpty()) {
                log.warn("Aircraft not found: {}", aircraftId);
                return null;
            }
            
            return buildAircraftDto(aircraftOpt.get());
            
        } catch (Exception e) {
            log.error("Error getting aircraft temperature details: {}", aircraftId, e);
            return null;
        }
    }
    
    public AircraftDto getAircraftTemperatureDetailsByAircraftId(String aircraftId) {
        try {
            Optional<Aircraft> aircraftOpt = aircraftRepository.findByAircraftId(aircraftId);
            if (aircraftOpt.isEmpty()) {
                log.warn("Aircraft not found: {}", aircraftId);
                return null;
            }
            
            return buildAircraftDto(aircraftOpt.get());
            
        } catch (Exception e) {
            log.error("Error getting aircraft temperature details: {}", aircraftId, e);
            return null;
        }
    }
    
    public List<TemperatureReadingDto> getAircraftTemperatureHistory(String aircraftId, int hours) {
        try {
            Optional<Aircraft> aircraftOpt = aircraftRepository.findByAircraftId(aircraftId);
            if (aircraftOpt.isEmpty()) {
                log.warn("Aircraft not found: {}", aircraftId);
                return new ArrayList<>();
            }
            
            LocalDateTime endTime = LocalDateTime.now();
            LocalDateTime startTime = endTime.minusHours(hours);
            
            List<CabinTemperatureReading> readings = temperatureReadingRepository
                .findReadingsByAircraftAndTimeRange(aircraftOpt.get().getId(), startTime, endTime);
            
            return readings.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
                
        } catch (Exception e) {
            log.error("Error getting aircraft temperature history for {}", aircraftId, e);
            return new ArrayList<>();
        }
    }
    
    public List<TemperatureReadingDto> getAircraftTemperatureTrends(String aircraftId, int hours) {
        List<TemperatureReadingDto> trends = getAircraftTemperatureHistory(aircraftId, hours);
        
        try {
            webSocketService.broadcastTrendData(aircraftId, trends);
            log.debug("Trend data broadcasted via WebSocket for aircraft: {}", aircraftId);
        } catch (Exception e) {
            log.warn("Failed to broadcast trend data via WebSocket for aircraft {}: {}", aircraftId, e.getMessage());
        }
        
        return trends;
    }

    public List<TemperatureReadingDto> getAircraftTemperatureTrends(String aircraftId) {
        return getAircraftTemperatureTrends(aircraftId, 7 * 24);
    }

    public List<AircraftDto> getAllAircraft() {
        return getFleetTemperatureOverview();
    }

    public Object getActiveAlarms() {
        try {
            return alarmService.getActiveAlarms();
        } catch (Exception e) {
            log.error("Error getting active alarms", e);
            return new ArrayList<>();
        }
    }

    public List<TemperatureReadingDto> getTemperaturesInRange(LocalDateTime startTime, LocalDateTime endTime) {
        try {
            List<CabinTemperatureReading> readings = temperatureReadingRepository
                .findReadingsByTimeRange(startTime, endTime);
            
            return readings.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
                
        } catch (Exception e) {
            log.error("Error getting temperatures in range from {} to {}", startTime, endTime, e);
            return new ArrayList<>();
        }
    }

    public List<TemperatureReadingDto> getTemperaturesInRange(String aircraftId, String timeRange) {
        int hours = 24; 
        if ("week".equals(timeRange)) {
            hours = 168;
        } else if ("month".equals(timeRange)) {
            hours = 720;
        }
        return getAircraftTemperatureHistory(aircraftId, hours);
    }

    public Object getTestAlarms() {
        try {
            return alarmService.getActiveAlarms();
        } catch (Exception e) {
            log.error("Error getting test alarms", e);
            return new ArrayList<>();
        }
    }

    public Object getTestData() {
        return getFleetTemperatureOverview();
    }

    public List<Object> getRouteTemperatureAnalysis() {
        try {
            List<Object> analysis = new ArrayList<>();
            List<Aircraft> activeAircraft = aircraftRepository.findActiveAircraft();
            
            for (Aircraft aircraft : activeAircraft) {
                Object routeAnalysis = createRouteAnalysis(aircraft);
                if (routeAnalysis != null) {
                    analysis.add(routeAnalysis);
                }
            }
            
            return analysis;
        } catch (Exception e) {
            log.error("Error getting route temperature analysis", e);
            return new ArrayList<>();
        }
    }

    public Object getRouteTemperatureAnalysisById(String routeId) {
        try {
            Optional<Aircraft> aircraftOpt = aircraftRepository.findByAircraftId(routeId);
            if (aircraftOpt.isEmpty()) {
                log.warn("Aircraft not found for route analysis: {}", routeId);
                return null;
            }
            
            return createRouteAnalysis(aircraftOpt.get());
            
        } catch (Exception e) {
            log.error("Error getting route temperature analysis for route {}", routeId, e);
            return null;
        }
    }

    private Object createRouteAnalysis(Aircraft aircraft) {
        try {
            List<CabinTemperatureReading> recentReadings = temperatureReadingRepository
                .findReadingsByAircraftAndTimeRange(aircraft.getId(), 
                    LocalDateTime.now().minusHours(24), LocalDateTime.now());
            
            if (recentReadings.isEmpty()) {
                return null;
            }
            
            double avgTemperature = recentReadings.stream()
                .mapToDouble(CabinTemperatureReading::getTemperatureCelsius)
                .average()
                .orElse(0.0);
            
            double maxTemperature = recentReadings.stream()
                .mapToDouble(CabinTemperatureReading::getTemperatureCelsius)
                .max()
                .orElse(0.0);
            
            double minTemperature = recentReadings.stream()
                .mapToDouble(CabinTemperatureReading::getTemperatureCelsius)
                .min()
                .orElse(0.0);
            
            AircraftInfo aircraftInfo = AIRCRAFT_INFO.get(aircraft.getAircraftId());
            String route = aircraftInfo != null ? aircraftInfo.getRoute() : "Unknown Route";
            
            return Map.of(
                "routeId", aircraft.getAircraftId(),
                "route", route,
                "aircraftId", aircraft.getId(),
                "manufacturer", aircraftInfo != null ? aircraftInfo.getManufacturer() : "Unknown",
                "model", aircraftInfo != null ? aircraftInfo.getModel() : "Unknown",
                "averageTemperature", avgTemperature,
                "maxTemperature", maxTemperature,
                "minTemperature", minTemperature,
                "readingCount", recentReadings.size(),
                "lastUpdated", LocalDateTime.now()
            );
            
        } catch (Exception e) {
            log.error("Error creating route analysis for aircraft {}", aircraft.getId(), e);
            return null;
        }
    }
    
    public AircraftDto getDashboardSummary() {
        try {
            List<Aircraft> activeAircraft = aircraftRepository.findActiveAircraft();
            if (activeAircraft.isEmpty()) {
                return null;
            }
            
            return buildAircraftDto(activeAircraft.get(0));
            
        } catch (Exception e) {
            log.error("Error getting dashboard summary", e);
            return null;
        }
    }
    
    private AircraftDto buildAircraftDto(Aircraft aircraft) {
        List<CabinZoneTemperatureDto> zoneTemperatures = new ArrayList<>();
        
        for (CabinZone zone : CabinZone.values()) {
            Optional<CabinTemperatureReading> latestReading = 
                temperatureReadingRepository.findLatestReadingByAircraftAndZone(aircraft.getId(), zone);
            
            if (latestReading.isPresent()) {
                CabinTemperatureReading reading = latestReading.get();
                zoneTemperatures.add(CabinZoneTemperatureDto.builder()
                    .zone(zone.name())
                    .temperatureCelsius(reading.getTemperatureCelsius())
                    .temperatureFahrenheit(reading.getTemperatureFahrenheit())
                    .status(reading.getStatus().name())
                    .lastUpdated(reading.getTimestamp())
                    .build());
            } else {
                zoneTemperatures.add(CabinZoneTemperatureDto.builder()
                    .zone(zone.name())
                    .temperatureCelsius(0.0)
                    .temperatureFahrenheit(32.0)
                    .status("UNKNOWN")
                    .lastUpdated(LocalDateTime.now())
                    .build());
            }
        }
        
        TemperatureStatusDto overallStatus = calculateOverallStatus(zoneTemperatures);
        
        AircraftInfo aircraftInfo = AIRCRAFT_INFO.get(aircraft.getAircraftId());
        
        return AircraftDto.builder()
            .id(aircraft.getId())
            .aircraftId(aircraft.getAircraftId())
            .manufacturer(aircraftInfo != null ? aircraftInfo.getManufacturer() : "Unknown")
            .model(aircraftInfo != null ? aircraftInfo.getModel() : "Unknown")
            .year(aircraftInfo != null ? aircraftInfo.getYear() : 2020)
            .registrationNumber(aircraftInfo != null ? aircraftInfo.getRegistrationNumber() : aircraft.getAircraftId())
            .route(aircraftInfo != null ? aircraftInfo.getRoute() : "Unknown Route")
            .status(aircraft.getStatus())
            .createdAt(aircraft.getCreatedAt())
            .updatedAt(aircraft.getUpdatedAt())
            .currentTemperatures(zoneTemperatures)
            .overallStatus(overallStatus)
            .build();
    }
    
    private TemperatureStatusDto calculateOverallStatus(List<CabinZoneTemperatureDto> zoneTemperatures) {
        int warningCount = 0;
        int criticalCount = 0;
        LocalDateTime lastUpdated = LocalDateTime.now();
        
        for (CabinZoneTemperatureDto zone : zoneTemperatures) {
            if ("WARNING".equals(zone.getStatus())) {
                warningCount++;
            } else if ("CRITICAL".equals(zone.getStatus())) {
                criticalCount++;
            }
            
            if (zone.getLastUpdated() != null && zone.getLastUpdated().isAfter(lastUpdated)) {
                lastUpdated = zone.getLastUpdated();
            }
        }
        
        String overallStatus = "NORMAL";
        if (criticalCount > 0) {
            overallStatus = "CRITICAL";
        } else if (warningCount > 0) {
            overallStatus = "WARNING";
        }
        
        return TemperatureStatusDto.builder()
            .overallStatus(overallStatus)
            .warningCount(warningCount)
            .criticalCount(criticalCount)
            .lastUpdated(lastUpdated)
            .build();
    }
    
    private TemperatureReadingDto convertToDto(CabinTemperatureReading reading) {
        AircraftInfo aircraftInfo = AIRCRAFT_INFO.get(reading.getAircraft().getAircraftId());
        
        return TemperatureReadingDto.builder()
                .id(reading.getId())
                .aircraftId(reading.getAircraft().getAircraftId())
                .cabinZone(reading.getCabinZone().name())
                .temperatureCelsius(reading.getTemperatureCelsius())
                .temperatureFahrenheit(reading.getTemperatureFahrenheit())
                .status(reading.getStatus().name())
                .route(aircraftInfo != null ? aircraftInfo.getRoute() : "Unknown")
                .timestamp(reading.getTimestamp())
                .build();
    }
}
