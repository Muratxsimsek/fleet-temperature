package com.fleet.temperature.exception;

public class DatabaseOperationException extends RuntimeException {
    
    private final String operation;
    private final String entityType;
    private final String entityId;
    
    public DatabaseOperationException(String message, String operation, String entityType, String entityId) {
        super(message);
        this.operation = operation;
        this.entityType = entityType;
        this.entityId = entityId;
    }
    
    public DatabaseOperationException(String message, String operation, String entityType, String entityId, Throwable cause) {
        super(message, cause);
        this.operation = operation;
        this.entityType = entityType;
        this.entityId = entityId;
    }
    
    public String getOperation() {
        return operation;
    }
    
    public String getEntityType() {
        return entityType;
    }
    
    public String getEntityId() {
        return entityId;
    }
}
