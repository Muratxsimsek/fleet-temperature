package com.fleet.temperature.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TemperatureAlarmDto {
    
    private Long id;
    private String aircraftId;
    private String cabinZone;
    private String status; 
    private Double temperatureCelsius;
    private String notes;
    private LocalDateTime timestamp;
    
    private Long readingId;
    private String severity;
    private Double thresholdTemperature;
    private String thresholdUnit;
    private LocalDateTime triggeredAt;
    private LocalDateTime resolvedAt;
    private String description;
    private LocalDateTime createdAt;
}
