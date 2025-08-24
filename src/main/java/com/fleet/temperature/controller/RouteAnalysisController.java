package com.fleet.temperature.controller;

import com.fleet.temperature.dto.RouteTemperatureAnalysisDto;
import com.fleet.temperature.service.RouteAnalysisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/route-analysis")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class RouteAnalysisController {
    
    private final RouteAnalysisService routeAnalysisService;
    
    @GetMapping("/routes/{routeCode}/temperature-trends")
    public RouteTemperatureAnalysisDto getRouteTemperatureTrends(
            @PathVariable String routeCode,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        try {
            return routeAnalysisService.analyzeRouteTemperatureTrends(routeCode, startTime, endTime);
        } catch (Exception e) {
            log.error("Error analyzing route temperature trends for route: {}", routeCode, e);
            throw new RuntimeException("Failed to analyze route temperature trends", e);
        }
    }
    
    @GetMapping("/routes/temperature-comparison")
    public List<RouteTemperatureAnalysisDto> compareRouteTemperatures(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        try {
            return routeAnalysisService.compareRouteTemperatures(startTime, endTime);
        } catch (Exception e) {
            log.error("Error comparing route temperatures", e);
            throw new RuntimeException("Failed to compare route temperatures", e);
        }
    }
    
    @GetMapping("/routes/{routeCode}/alarm-statistics")
    public Object getRouteAlarmStatistics(
            @PathVariable String routeCode,
            @RequestParam(defaultValue = "30") int days) {
        try {
            return routeAnalysisService.getRouteAlarmStatistics(routeCode, days);
        } catch (Exception e) {
            log.error("Error getting alarm statistics for route: {}", routeCode, e);
            throw new RuntimeException("Failed to retrieve route alarm statistics", e);
        }
    }
}
