package com.fleet.temperature.controller;

import com.fleet.temperature.dto.TemperatureReadingDto;
import com.fleet.temperature.dto.AircraftDto;
import com.fleet.temperature.dto.TemperatureAlarmDto;
import com.fleet.temperature.service.WebSocketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
@Slf4j
public class WebSocketController {
    
    private final WebSocketService webSocketService;
    
    @MessageMapping("/request-temperature")
    @SendTo("/topic/temperature-response")
    public TemperatureReadingDto handleTemperatureRequest(String aircraftId) {
        log.info("Temperature request received for aircraft: {}", aircraftId);
        return null;
    }
    
    @MessageMapping("/request-dashboard")
    @SendTo("/topic/dashboard-response")
    public AircraftDto handleDashboardRequest(String request) {
        log.info("Dashboard request received: {}", request);
        return null;
    }
    
    @MessageMapping("/request-trends")
    @SendToUser("/queue/trends-response")
    public Object handleTrendRequest(String aircraftId) {
        log.info("Trend request received for aircraft: {}", aircraftId);
        return null;
    }
}
