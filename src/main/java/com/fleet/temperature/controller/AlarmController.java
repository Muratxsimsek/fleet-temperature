package com.fleet.temperature.controller;

import com.fleet.temperature.persistence.entity.TemperatureAlarm;
import com.fleet.temperature.service.TemperatureAlarmService;
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

import java.util.List;

@RestController
@RequestMapping("/api/alarms")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
@Tag(name = "Temperature Alarms", description = "Temperature alarm management endpoints")
public class AlarmController {

    private final TemperatureAlarmService alarmService;

    @GetMapping("/active")
    @Operation(summary = "Get all active temperature alarms",
               description = "Retrieves a list of all currently active temperature alarms across the fleet")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved active alarms",
                    content = @Content(schema = @Schema(implementation = TemperatureAlarm.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public List<TemperatureAlarm> getActiveAlarms() {
        log.info("Getting active temperature alarms");
        return alarmService.getActiveAlarms();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get alarm by ID",
               description = "Retrieves a specific temperature alarm by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved alarm",
                    content = @Content(schema = @Schema(implementation = TemperatureAlarm.class))),
        @ApiResponse(responseCode = "404", description = "Alarm not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public TemperatureAlarm getAlarmById(@PathVariable @Parameter(description = "Alarm ID") Long id) {
        log.info("Getting alarm by ID: {}", id);
        return alarmService.getAlarmById(id);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete alarm",
               description = "Deletes a temperature alarm by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Alarm deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Alarm not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public void deleteAlarm(@PathVariable @Parameter(description = "Alarm ID") Long id) {
        log.info("Deleting alarm with ID: {}", id);
        alarmService.deleteAlarm(id);
    }

    @GetMapping("/aircraft/{aircraftId}/count")
    @Operation(summary = "Get active alarm count by aircraft",
               description = "Retrieves the count of active alarms for a specific aircraft")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved alarm count",
                    content = @Content(schema = @Schema(implementation = Long.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public Long getActiveAlarmCountByAircraft(@PathVariable @Parameter(description = "Aircraft ID") Long aircraftId) {
        log.info("Getting active alarm count for aircraft: {}", aircraftId);
        return alarmService.getActiveAlarmCountByAircraft(aircraftId);
    }
}
