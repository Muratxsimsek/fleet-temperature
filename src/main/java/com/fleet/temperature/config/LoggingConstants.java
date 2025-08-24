package com.fleet.temperature.config;

public final class LoggingConstants {
    

    
    public static final String ERROR_PREFIX = "ERROR";
    public static final String WARN_PREFIX = "WARN";
    public static final String INFO_PREFIX = "INFO";
    
    public static final String KAFKA_ERROR_TEMPLATE = "Kafka processing failed - MessageId: {}, Topic: {}, Error: {}";
    public static final String DATABASE_ERROR_TEMPLATE = "Database operation failed - Operation: {}, Entity: {}, ID: {}, Error: {}";
    public static final String WEBSOCKET_ERROR_TEMPLATE = "WebSocket error - Session: {}, Destination: {}, Type: {}, Error: {}";
    
    public static final String BATCH_PROCESSING_START = "Starting batch processing - Size: {}, Topic: {}";
    public static final String BATCH_PROCESSING_SUCCESS = "Batch processing completed successfully - Size: {}, Duration: {}ms";
    public static final String BATCH_PROCESSING_FAILURE = "Batch processing failed - Size: {}, Error: {}";
    
    public static final String MESSAGE_PROCESSING_START = "Processing message - ID: {}, Aircraft: {}";
    public static final String MESSAGE_PROCESSING_SUCCESS = "Message processed successfully - ID: {}, Aircraft: {}";
    public static final String MESSAGE_PROCESSING_FAILURE = "Message processing failed - ID: {}, Aircraft: {}, Error: {}";
    
    public static final String DLQ_SEND_SUCCESS = "Message sent to DLQ - ID: {}, Topic: {}, Error: {}";
    public static final String DLQ_SEND_FAILURE = "Failed to send message to DLQ - ID: {}, Error: {}";
    
    public static final String ALARM_CREATION_SUCCESS = "Temperature alarm created - ID: {}, Aircraft: {}, Zone: {}, Severity: {}";
    public static final String ALARM_CREATION_FAILURE = "Failed to create temperature alarm - Aircraft: {}, Zone: {}, Error: {}";
}
