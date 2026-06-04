package com.supplysense.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.supplysense.processor.RiskEventProcessor;
import com.supplysense.processor.SupplierEventProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventConsumer {

    private final RiskEventProcessor riskEventProcessor;
    private final SupplierEventProcessor supplierEventProcessor;
    private final ObjectMapper objectMapper;

    @KafkaListener(
        topics = "risk.score.updates",
        groupId = "event-processor-risk",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeRiskScoreUpdate(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> event = objectMapper.readValue(message, Map.class);
            log.debug("Risk score update received: topic={}, partition={}, offset={}", topic, partition, offset);
            riskEventProcessor.processRiskScoreUpdate(event);
        } catch (Exception e) {
            log.error("Failed to process risk score update: {}", e.getMessage(), e);
        }
    }

    @KafkaListener(
        topics = "risk.alerts",
        groupId = "event-processor-alerts",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeRiskAlert(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> event = objectMapper.readValue(message, Map.class);
            log.info("Risk alert event received: {}", event.get("event"));
            riskEventProcessor.processAlertEvent(event);
        } catch (Exception e) {
            log.error("Failed to process risk alert: {}", e.getMessage(), e);
        }
    }

    @KafkaListener(
        topics = "supplier.events",
        groupId = "event-processor-suppliers",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeSupplierEvent(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> event = objectMapper.readValue(message, Map.class);
            log.info("Supplier event received: {}", event.get("event"));
            supplierEventProcessor.processSupplierEvent(event);
        } catch (Exception e) {
            log.error("Failed to process supplier event: {}", e.getMessage(), e);
        }
    }

    @KafkaListener(
        topics = "risk.recalculation.trigger",
        groupId = "event-processor-recalc"
    )
    public void consumeRecalculationTrigger(@Payload String message) {
        log.info("Risk recalculation triggered");
        riskEventProcessor.processRecalculationTrigger();
    }
}
