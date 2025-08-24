package com.fleet.temperature.exception;

public class KafkaProcessingException extends RuntimeException {
    
    private final String messageId;
    private final String topic;
    private final String errorCode;
    
    public KafkaProcessingException(String message, String messageId, String topic, String errorCode) {
        super(message);
        this.messageId = messageId;
        this.topic = topic;
        this.errorCode = errorCode;
    }
    
    public KafkaProcessingException(String message, String messageId, String topic, String errorCode, Throwable cause) {
        super(message, cause);
        this.messageId = messageId;
        this.topic = topic;
        this.errorCode = errorCode;
    }
    
    public String getMessageId() {
        return messageId;
    }
    
    public String getTopic() {
        return topic;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
}
