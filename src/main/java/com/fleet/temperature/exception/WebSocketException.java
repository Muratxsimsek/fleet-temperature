package com.fleet.temperature.exception;

public class WebSocketException extends RuntimeException {
    
    private final String sessionId;
    private final String destination;
    private final String errorType;
    
    public WebSocketException(String message, String sessionId, String destination, String errorType) {
        super(message);
        this.sessionId = sessionId;
        this.destination = destination;
        this.errorType = errorType;
    }
    
    public WebSocketException(String message, String sessionId, String destination, String errorType, Throwable cause) {
        super(message, cause);
        this.sessionId = sessionId;
        this.destination = destination;
        this.errorType = errorType;
    }
    
    public String getSessionId() {
        return sessionId;
    }
    
    public String getDestination() {
        return destination;
    }
    
    public String getErrorType() {
        return errorType;
    }
}
