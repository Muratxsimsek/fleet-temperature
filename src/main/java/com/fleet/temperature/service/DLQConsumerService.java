package com.fleet.temperature.service;

import com.fleet.temperature.config.KafkaConstants;
import com.fleet.temperature.config.LoggingConstants;
import com.fleet.temperature.dto.DLQMessage;
import com.fleet.temperature.dto.VHFMessageDto;
import com.fleet.temperature.exception.KafkaProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
@RequiredArgsConstructor
public class DLQConsumerService {

    private final VHFMessageProcessorService messageProcessorService;
    private final KafkaTemplate<String, DLQMessage> kafkaTemplate;
    
    private static final int MAX_RETRIES = 3;

    @KafkaListener(
        topics = KafkaConstants.DLQ_TOPIC,
        groupId = KafkaConstants.CONSUMER_GROUP_DLQ
    )
    public void consumeDLQMessage(
        @Payload DLQMessage dlqMessage,
        @Header(KafkaHeaders.ACKNOWLEDGMENT) Acknowledgment acknowledgment
    ) {
        try {
            log.info("Processing DLQ message - ID: {}", dlqMessage.getOriginalMessage().getMessageId());
            processDLQMessage(dlqMessage);
            acknowledgment.acknowledge();
            log.info("Successfully processed DLQ message - ID: {}", dlqMessage.getOriginalMessage().getMessageId());
        } catch (Exception e) {
            log.error("Failed to process DLQ message - ID: {}, Error: {}", dlqMessage.getOriginalMessage().getMessageId(), e.getMessage());
            handleDLQProcessingError(dlqMessage, e);
            acknowledgment.acknowledge();
        }
    }



    private void processDLQMessage(DLQMessage dlqMessage) {
        VHFMessageDto originalMessage = dlqMessage.getOriginalMessage();
        int currentRetryCount = dlqMessage.getRetryCount();
        
        if (currentRetryCount >= MAX_RETRIES) {
            log.warn("Max retries exceeded for message: {} (attempts: {})", originalMessage.getMessageId(), currentRetryCount);
            sendToFinalDLQ(dlqMessage);
            return;
        }
        
        try {
            messageProcessorService.processVHFMessage(originalMessage);
            log.info("Retry successful for message: {} (attempt: {})", originalMessage.getMessageId(), currentRetryCount + 1);
        } catch (Exception e) {
            log.error("Retry failed for message: {} (attempt: {}), Error: {}", originalMessage.getMessageId(), currentRetryCount + 1, e.getMessage());
            incrementRetryCountAndResend(dlqMessage, e);
        }
    }



    private void handleDLQProcessingError(DLQMessage dlqMessage, Exception error) {
        log.error("Critical error processing DLQ message: {}", dlqMessage.getOriginalMessage().getMessageId(), error);
        sendToFinalDLQ(dlqMessage);
    }

    private void incrementRetryCountAndResend(DLQMessage dlqMessage, Exception error) {
        DLQMessage retryMessage = createRetryMessage(dlqMessage, error);
        try {
            kafkaTemplate.send(KafkaConstants.DLQ_TOPIC, 
                retryMessage.getOriginalMessage().getMessageId(), retryMessage);
            log.info("Retry message sent to DLQ - ID: {}, Retry Count: {}", retryMessage.getOriginalMessage().getMessageId(), retryMessage.getRetryCount());
        } catch (Exception e) {
            log.error("Failed to send retry message to DLQ: {}", retryMessage.getOriginalMessage().getMessageId(), e);
            throw new KafkaProcessingException(
                "Failed to send retry message to DLQ",
                retryMessage.getOriginalMessage().getMessageId(),
                KafkaConstants.DLQ_TOPIC,
                "RETRY_SEND_FAILURE",
                e
            );
        }
    }



    private void sendToFinalDLQ(DLQMessage dlqMessage) {
        try {
            DLQMessage finalDLQMessage = createFinalDLQMessage(dlqMessage);
            kafkaTemplate.send(KafkaConstants.FINAL_DLQ_TOPIC, 
                finalDLQMessage.getOriginalMessage().getMessageId(), finalDLQMessage);
            log.info("Message sent to final DLQ - ID: {}", finalDLQMessage.getOriginalMessage().getMessageId());
        } catch (Exception e) {
            log.error("Failed to send message to final DLQ: {}", dlqMessage.getOriginalMessage().getMessageId(), e);
            throw new KafkaProcessingException(
                "Failed to send message to final DLQ",
                dlqMessage.getOriginalMessage().getMessageId(),
                KafkaConstants.FINAL_DLQ_TOPIC,
                "FINAL_DLQ_SEND_FAILURE",
                e
            );
        }
    }



    private DLQMessage createRetryMessage(DLQMessage originalDLQMessage, Exception error) {
        return DLQMessage.builder()
            .originalMessage(originalDLQMessage.getOriginalMessage())
            .errorMessage(error.getMessage())
            .errorStackTrace(getStackTrace(error))
            .timestamp(LocalDateTime.now())
            .retryCount(originalDLQMessage.getRetryCount() + 1)
            .build();
    }

    private DLQMessage createFinalDLQMessage(DLQMessage originalDLQMessage) {
        return DLQMessage.builder()
            .originalMessage(originalDLQMessage.getOriginalMessage())
            .errorMessage("Max retries exceeded: " + originalDLQMessage.getErrorMessage())
            .errorStackTrace(originalDLQMessage.getErrorStackTrace())
            .timestamp(LocalDateTime.now())
            .retryCount(originalDLQMessage.getRetryCount())
            .build();
    }

    private String getStackTrace(Exception error) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        error.printStackTrace(pw);
        return sw.toString();
    }
}
