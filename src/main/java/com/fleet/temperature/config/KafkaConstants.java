package com.fleet.temperature.config;

public final class KafkaConstants {
    

    
    public static final String MAIN_TOPIC = "fleet.cabin_temperature.raw";
    public static final String DLQ_TOPIC = "fleet.cabin_temperature.raw.dlq";
    public static final String FINAL_DLQ_TOPIC = "fleet.cabin_temperature.raw.final-dlq";

    public static final String CONSUMER_GROUP = "fleet-temperature-group";
    public static final String CONSUMER_GROUP_DLQ = "fleet-temperature-group.dlq";

    public static final int DEFAULT_BATCH_SIZE = 100;
    public static final int MAX_POLL_RECORDS = 5000;
    public static final int FETCH_MIN_BYTES = 16384;
    public static final int FETCH_MAX_WAIT_MS = 100;
    public static final int MAX_RETRY_ATTEMPTS = 3;
    public static final int POLL_TIMEOUT = 500;
    public static final int IDLE_BETWEEN_POLLS = 50;
    
    public static final String ERROR_CODE_PROCESSING_ERROR = "PROCESSING_ERROR";
    public static final String ERROR_CODE_DESERIALIZATION_ERROR = "DESERIALIZATION_ERROR";
    public static final String ERROR_CODE_DATABASE_ERROR = "DATABASE_ERROR";
    public static final String ERROR_CODE_RETRY_EXHAUSTED = "RETRY_EXHAUSTED";
    
    public static final String DLQ_ADDITIONAL_INFO_BATCH_FAILURE = "Message processing failed in batch consumer";
    public static final String DLQ_ADDITIONAL_INFO_SINGLE_FAILURE = "Message processing failed in single consumer";
}
