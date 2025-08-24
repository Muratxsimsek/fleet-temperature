package com.fleet.temperature.dto;

import com.fleet.temperature.persistence.enums.AircraftStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AircraftDto {
    
    private Long id;
    private String aircraftId;
    private String manufacturer;
    private String model;
    private Integer year;
    private String registrationNumber;
    private String route;
    private AircraftStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<CabinZoneTemperatureDto> currentTemperatures;
    private TemperatureStatusDto overallStatus;
    
    @Data
@Getter
@Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CabinZoneTemperatureDto {
        private String zone;
        private Double temperatureCelsius;
        private Double temperatureFahrenheit;
        private String status;
        private LocalDateTime lastUpdated;
    }
    
    @Data
@Getter
@Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TemperatureStatusDto {
        private String overallStatus;
        private Integer warningCount;
        private Integer criticalCount;
        private LocalDateTime lastUpdated;
    }
}
