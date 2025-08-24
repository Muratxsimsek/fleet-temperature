package com.fleet.temperature.service;

import com.fleet.temperature.dto.RouteTemperatureAnalysisDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RouteAnalysisService {
    
    public RouteTemperatureAnalysisDto analyzeRouteTemperatureTrends(String routeCode, LocalDateTime startTime, LocalDateTime endTime) {
        log.info("Analyzing temperature trends for route: {} from {} to {}", routeCode, startTime, endTime);
        
        return RouteTemperatureAnalysisDto.builder()
            .routeCode(routeCode)
            .origin("IST")
            .destination("JFK")
            .averageTemperature(22.5)
            .maxTemperature(28.0)
            .minTemperature(18.0)
            .alarmCount(5)
            .warningCount(3)
            .criticalCount(2)
            .analysisStartTime(startTime)
            .analysisEndTime(endTime)
            .lastUpdated(LocalDateTime.now())
            .build();
    }
    
    public List<RouteTemperatureAnalysisDto> compareRouteTemperatures(LocalDateTime startTime, LocalDateTime endTime) {
        log.info("Comparing route temperatures from {} to {}", startTime, endTime);
        
        List<RouteTemperatureAnalysisDto> comparison = new ArrayList<>();
        
        comparison.add(RouteTemperatureAnalysisDto.builder()
            .routeCode("IST-JFK")
            .origin("IST")
            .destination("JFK")
            .averageTemperature(22.5)
            .maxTemperature(28.0)
            .minTemperature(18.0)
            .alarmCount(5)
            .warningCount(3)
            .criticalCount(2)
            .analysisStartTime(startTime)
            .analysisEndTime(endTime)
            .lastUpdated(LocalDateTime.now())
            .build());
            
        comparison.add(RouteTemperatureAnalysisDto.builder()
            .routeCode("LHR-CDG")
            .origin("LHR")
            .destination("CDG")
            .averageTemperature(20.0)
            .maxTemperature(25.0)
            .minTemperature(16.0)
            .alarmCount(3)
            .warningCount(2)
            .criticalCount(1)
            .analysisStartTime(startTime)
            .analysisEndTime(endTime)
            .lastUpdated(LocalDateTime.now())
            .build());
            
        return comparison;
    }
    
    public Object getRouteAlarmStatistics(String routeCode, int days) {
        log.info("Getting alarm statistics for route: {} for last {} days", routeCode, days);
        
        return new Object() {
            public final String routeCodeValue = routeCode;
            public final int totalAlarms = 15;
            public final int warningAlarms = 10;
            public final int criticalAlarms = 5;
            public final double averageTemperature = 23.5;
            public final LocalDateTime lastAlarm = LocalDateTime.now().minusHours(2);
        };
    }
}
