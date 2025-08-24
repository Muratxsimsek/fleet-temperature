package com.fleet.temperature.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VHFMessageDto {
    
    private String messageId;
    
    private String aircraftId;
    
    private String sensorType;
    
    private String schemaVersion;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Instant ts;
    
    private TemperaturePayloadDto payload;
    
    @Data
@Getter
@Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TemperaturePayloadDto {
        
        private String cabinZone;
        
        private Double temperatureC;
    }
}
