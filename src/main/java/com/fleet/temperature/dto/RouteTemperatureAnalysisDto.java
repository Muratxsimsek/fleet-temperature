package com.fleet.temperature.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RouteTemperatureAnalysisDto {
    
    private String routeCode;
    private String origin;
    private String destination;
    private Double averageTemperature;
    private Double maxTemperature;
    private Double minTemperature;
    private Integer alarmCount;
    private Integer warningCount;
    private Integer criticalCount;
    private LocalDateTime analysisStartTime;
    private LocalDateTime analysisEndTime;
    private LocalDateTime lastUpdated;
}
