package com.fleet.temperature.controller;

import com.fleet.temperature.dto.AircraftDto;
import com.fleet.temperature.dto.RouteTemperatureAnalysisDto;
import com.fleet.temperature.dto.TemperatureReadingDto;
import com.fleet.temperature.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
@Tag(name = "Dashboard", description = "Dashboard data endpoints")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/aircraft/{aircraftId}/temperatures")
    @Operation(summary = "Get aircraft temperature history",
               description = "Retrieves temperature history for a specific aircraft within specified hours")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved temperature history",
                    content = @Content(schema = @Schema(implementation = TemperatureReadingDto.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public List<TemperatureReadingDto> getAircraftTemperatures(
            @PathVariable String aircraftId,
            @RequestParam(defaultValue = "24") int hours) {
        try {
            return dashboardService.getAircraftTemperatureHistory(aircraftId, hours);
        } catch (Exception e) {
            log.error("Error getting aircraft temperatures for {}", aircraftId, e);
            throw new RuntimeException("Failed to retrieve temperature history: " + e.getMessage());
        }
    }

    @GetMapping("/aircraft/{aircraftId}/trends")
    @Operation(summary = "Get aircraft temperature trends",
               description = "Retrieves temperature trends and patterns for a specific aircraft")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved temperature trends",
                    content = @Content(schema = @Schema(implementation = Object.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public Object getAircraftTrends(@PathVariable String aircraftId) {
        try {
            return dashboardService.getAircraftTemperatureTrends(aircraftId);
        } catch (Exception e) {
            log.error("Error getting aircraft trends for {}", aircraftId, e);
            throw new RuntimeException("Failed to retrieve temperature trends: " + e.getMessage());
        }
    }

    @GetMapping("/aircraft")
    @Operation(summary = "Get all aircraft",
               description = "Retrieves a list of all aircraft in the fleet")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved aircraft list",
                    content = @Content(schema = @Schema(implementation = AircraftDto.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public List<AircraftDto> getAllAircraft() {
        try {
            return dashboardService.getAllAircraft();
        } catch (Exception e) {
            log.error("Error getting all aircraft", e);
            throw new RuntimeException("Failed to retrieve aircraft list: " + e.getMessage());
        }
    }

    @GetMapping("/alarms/active")
    @Operation(summary = "Get active alarms",
               description = "Retrieves all currently active temperature alarms")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved active alarms",
                    content = @Content(schema = @Schema(implementation = Object.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public Object getActiveAlarms() {
        try {
            return dashboardService.getActiveAlarms();
        } catch (Exception e) {
            log.error("Error getting active alarms", e);
            throw new RuntimeException("Failed to retrieve active alarms: " + e.getMessage());
        }
    }

    @GetMapping("/summary")
    @Operation(summary = "Get dashboard summary",
               description = "Retrieves a comprehensive summary of fleet temperature data")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved dashboard summary",
                    content = @Content(schema = @Schema(implementation = Object.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public Object getDashboardSummary() {
        try {
            return dashboardService.getDashboardSummary();
        } catch (Exception e) {
            log.error("Error getting dashboard summary", e);
            throw new RuntimeException("Failed to retrieve dashboard summary: " + e.getMessage());
        }
    }

    @GetMapping("/temperatures/range")
    @Operation(summary = "Get temperatures in time range",
               description = "Retrieves temperature readings within a specified time range")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved temperature readings",
                    content = @Content(schema = @Schema(implementation = TemperatureReadingDto.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public List<TemperatureReadingDto> getTemperaturesInRange(
            @RequestParam LocalDateTime startTime,
            @RequestParam LocalDateTime endTime) {
        try {
            return dashboardService.getTemperaturesInRange(startTime, endTime);
        } catch (Exception e) {
            log.error("Error getting temperatures in range from {} to {}", startTime, endTime, e);
            throw new RuntimeException("Failed to retrieve temperature readings: " + e.getMessage());
        }
    }

    @GetMapping("/test/alarms")
    @Operation(summary = "Get test alarms",
               description = "Retrieves test alarm data for development purposes")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved test alarms",
                    content = @Content(schema = @Schema(implementation = Object.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public Object getTestAlarms() {
        try {
            return dashboardService.getTestAlarms();
        } catch (Exception e) {
            log.error("Error getting test alarms", e);
            throw new RuntimeException("Failed to retrieve test alarms: " + e.getMessage());
        }
    }

    @GetMapping("/test/data")
    @Operation(summary = "Get test data",
               description = "Retrieves test data for development purposes")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved test data",
                    content = @Content(schema = @Schema(implementation = Object.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public Object getTestData() {
        try {
            return dashboardService.getTestData();
        } catch (Exception e) {
            log.error("Error getting test data", e);
            throw new RuntimeException("Failed to retrieve test data: " + e.getMessage());
        }
    }

    @GetMapping("/route-analysis")
    @Operation(summary = "Get route temperature analysis",
               description = "Retrieves temperature analysis by route")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved route analysis",
                    content = @Content(schema = @Schema(implementation = Object.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public Object getRouteTemperatureAnalysis() {
        try {
            return dashboardService.getRouteTemperatureAnalysis();
        } catch (Exception e) {
            log.error("Error getting route temperature analysis", e);
            throw new RuntimeException("Failed to retrieve route analysis: " + e.getMessage());
        }
    }

    @GetMapping("/route-analysis/{routeId}")
    @Operation(summary = "Get route temperature analysis by route ID",
               description = "Retrieves temperature analysis for a specific route")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved route analysis",
                    content = @Content(schema = @Schema(implementation = Object.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public Object getRouteTemperatureAnalysisById(
            @PathVariable @Parameter(description = "Route ID") String routeId) {
        try {
            return dashboardService.getRouteTemperatureAnalysisById(routeId);
        } catch (Exception e) {
            log.error("Error getting route temperature analysis for route {}", routeId, e);
            throw new RuntimeException("Failed to retrieve route analysis: " + e.getMessage());
        }
    }
}
