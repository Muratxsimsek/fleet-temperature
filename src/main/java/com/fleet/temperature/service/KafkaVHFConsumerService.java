package com.fleet.temperature.service;

import com.fleet.temperature.config.KafkaConstants;
import com.fleet.temperature.config.LoggingConstants;
import com.fleet.temperature.dto.VHFMessageDto;
import com.fleet.temperature.exception.KafkaProcessingException;
import com.fleet.temperature.exception.DatabaseOperationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import com.fleet.temperature.dto.DLQMessage;

import java.time.LocalDateTime;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

@Slf4j
@Service
public class KafkaVHFConsumerService {

    private final VHFMessageProcessorService messageProcessorService;
    private final KafkaTemplate<String, DLQMessage> kafkaTemplate;

    public KafkaVHFConsumerService(
            VHFMessageProcessorService messageProcessorService,
            @Qualifier("dlqKafkaTemplate") KafkaTemplate<String, DLQMessage> kafkaTemplate) {
        this.messageProcessorService = messageProcessorService;
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(
        topics = KafkaConstants.MAIN_TOPIC,
        groupId = KafkaConstants.CONSUMER_GROUP,
        batch = "true"
    )
    @Async("kafkaConsumerExecutor")
    public void consumeVHFMessageBatch(
        @Payload List<VHFMessageDto> messages,
        @Header(KafkaHeaders.ACKNOWLEDGMENT) Acknowledgment acknowledgment
    ) {
        long startTime = System.currentTimeMillis();
        int batchSize = messages.size();
        
        try {
            log.info(LoggingConstants.BATCH_PROCESSING_START, batchSize, KafkaConstants.MAIN_TOPIC);
            processBatchMessages(messages);
            long duration = System.currentTimeMillis() - startTime;
            log.info(LoggingConstants.BATCH_PROCESSING_SUCCESS, batchSize, duration);
            acknowledgment.acknowledge();
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error(LoggingConstants.BATCH_PROCESSING_FAILURE, batchSize, e.getMessage());
            handleBatchProcessingError(messages, e);
            acknowledgment.acknowledge();
        }
    }

    private void logBatchProcessingStart(int batchSize, String topic) {
        log.info(LoggingConstants.BATCH_PROCESSING_START, batchSize, topic);
    }

    private void logBatchProcessingSuccess(int batchSize, long duration) {
        log.info(LoggingConstants.BATCH_PROCESSING_SUCCESS, batchSize, duration);
    }

    private void logBatchProcessingFailure(int batchSize, String error) {
        log.error(LoggingConstants.BATCH_PROCESSING_FAILURE, batchSize, error);
    }

    private void processBatchMessages(List<VHFMessageDto> messages) {
        messageProcessorService.processBatchMessages(messages);
    }





    private void handleBatchProcessingError(List<VHFMessageDto> messages, Exception error) {
        log.error("Critical error in batch processing, sending all messages to DLQ", error);
        for (VHFMessageDto message : messages) {
            sendToDeadLetterQueue(message, error);
        }
    }

    private void sendAircraftMessagesToDLQ(List<VHFMessageDto> messages, Exception error) {
        for (VHFMessageDto message : messages) {
            sendToDeadLetterQueue(message, error);
        }
    }

    private void sendToDeadLetterQueue(VHFMessageDto message, Exception error) {
        try {
            DLQMessage dlqMessage = createDLQMessage(message, error);
            kafkaTemplate.send(KafkaConstants.DLQ_TOPIC, message.getMessageId(), dlqMessage);
            log.info(LoggingConstants.DLQ_SEND_SUCCESS, message.getMessageId(), KafkaConstants.DLQ_TOPIC, error.getMessage());
        } catch (Exception e) {
            log.error(LoggingConstants.DLQ_SEND_FAILURE, message.getMessageId(), e.getMessage());
            throw new KafkaProcessingException(
                "Failed to send message to DLQ",
                message.getMessageId(),
                KafkaConstants.DLQ_TOPIC,
                "DLQ_SEND_FAILURE",
                e
            );
        }
    }



    private DLQMessage createDLQMessage(VHFMessageDto originalMessage, Exception error) {
        return DLQMessage.builder()
            .originalMessage(originalMessage)
            .errorMessage(error.getMessage())
            .errorStackTrace(getStackTrace(error))
            .timestamp(LocalDateTime.now())
            .retryCount(0)
            .build();
    }

    private String getStackTrace(Exception error) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        error.printStackTrace(pw);
        return sw.toString();
    }
}
