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
public class DLQMessage {
    
    private VHFMessageDto originalMessage;
    
    private String errorMessage;
    
    private String errorStackTrace;
    
    private LocalDateTime timestamp;
    
    private Integer retryCount;
    
    private String errorType;
    
    private String errorCode;
    
    private String additionalInfo;
}
