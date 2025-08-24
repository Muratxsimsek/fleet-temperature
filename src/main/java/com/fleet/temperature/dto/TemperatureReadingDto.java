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
public class TemperatureReadingDto {
    
    private Long id;
    private String aircraftId;
    private String messageId;
    private String cabinZone;
    private Double temperatureCelsius;
    private Double temperatureFahrenheit;
    private String status;
    private String route;
    private LocalDateTime timestamp;
    private String notes;
    private LocalDateTime createdAt;
}
