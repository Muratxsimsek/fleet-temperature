package com.fleet.temperature.service;

import com.fleet.temperature.dto.AircraftDto;
import com.fleet.temperature.dto.TemperatureReadingDto;
import com.fleet.temperature.dto.TemperatureAlarmDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebSocketService {
    
    private final SimpMessagingTemplate messagingTemplate;
    
    public void broadcastTemperatureUpdate(TemperatureReadingDto temperatureReading) {
        try {
            messagingTemplate.convertAndSend("/topic/temperature-updates", temperatureReading);
            log.debug("Temperature update broadcasted: {}", temperatureReading.getAircraftId());
        } catch (Exception e) {
            log.error("Error broadcasting temperature update", e);
        }
    }
    
    public void sendAircraftTemperature(String aircraftId, TemperatureReadingDto temperatureReading) {
        try {
            messagingTemplate.convertAndSend("/topic/aircraft/" + aircraftId + "/temperature", temperatureReading);
            log.debug("Aircraft temperature sent to {}: {}", aircraftId, temperatureReading.getTemperatureCelsius());
        } catch (Exception e) {
            log.error("Error sending aircraft temperature for {}", aircraftId, e);
        }
    }
    
    public void broadcastTemperatureAlarm(TemperatureAlarmDto alarm) {
        try {
            messagingTemplate.convertAndSend("/topic/temperature-alarms", alarm);
            log.debug("Temperature alarm broadcasted: {}", alarm.getAircraftId());
        } catch (Exception e) {
            log.error("Error broadcasting temperature alarm", e);
        }
    }
    
    public void broadcastDashboardSummary(AircraftDto dashboardData) {
        try {
            messagingTemplate.convertAndSend("/topic/dashboard-summary", dashboardData);
            log.debug("Dashboard summary broadcasted");
        } catch (Exception e) {
            log.error("Error broadcasting dashboard summary", e);
        }
    }
    
    public void broadcastTrendData(String aircraftId, Object trendData) {
        try {
            messagingTemplate.convertAndSend("/topic/trends/" + aircraftId, trendData);
            log.debug("Trend data sent for aircraft: {}", aircraftId);
        } catch (Exception e) {
            log.error("Error sending trend data for {}", aircraftId, e);
        }
    }
}
