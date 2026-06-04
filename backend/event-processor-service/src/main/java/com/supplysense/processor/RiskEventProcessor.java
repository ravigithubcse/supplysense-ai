package com.supplysense.processor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class RiskEventProcessor {

    private final SimpMessagingTemplate messagingTemplate;

    private static final String WS_TOPIC_RISK_UPDATES = "/topic/risk-updates";
    private static final String WS_TOPIC_ALERTS       = "/topic/alerts";

    /**
     * Fan-out risk score updates to connected WebSocket clients in real time.
     */
    public void processRiskScoreUpdate(Map<String, Object> event) {
        String supplierId = (String) event.get("supplierId");
        String orgId      = (String) event.get("orgId");

        if (supplierId == null || orgId == null) {
            log.warn("Risk score update missing supplierId or orgId: {}", event);
            return;
        }

        // Broadcast to all subscribers for this org
        messagingTemplate.convertAndSend(
                WS_TOPIC_RISK_UPDATES + "/" + orgId,
                Map.of(
                    "type",        "RISK_SCORE_UPDATE",
                    "supplierId",  supplierId,
                    "score",       event.getOrDefault("score", 0.0),
                    "riskLevel",   event.getOrDefault("riskLevel", "UNKNOWN"),
                    "timestamp",   System.currentTimeMillis()
                )
        );

        log.debug("Risk score broadcasted via WS: supplier={}, score={}", supplierId, event.get("score"));
    }

    /**
     * Broadcast alert events (CREATED / RESOLVED) over WebSocket.
     */
    public void processAlertEvent(Map<String, Object> event) {
        String eventType  = (String) event.get("event");
        String supplierId = (String) event.get("supplierId");
        String orgId      = (String) event.get("orgId");

        messagingTemplate.convertAndSend(
                WS_TOPIC_ALERTS + (orgId != null ? "/" + orgId : ""),
                Map.of(
                    "type",        eventType,
                    "supplierId",  supplierId != null ? supplierId : "",
                    "severity",    event.getOrDefault("severity", "INFO"),
                    "timestamp",   System.currentTimeMillis()
                )
        );

        log.info("Alert event broadcasted via WS: type={}", eventType);
    }

    /**
     * Handle scheduled recalculation triggers.
     */
    public void processRecalculationTrigger() {
        log.info("Processing risk recalculation trigger – notifying all connected clients");
        messagingTemplate.convertAndSend("/topic/system",
                Map.of("type", "RECALCULATION_STARTED", "timestamp", System.currentTimeMillis()));
    }
}
