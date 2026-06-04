package com.supplysense.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.supplysense.model.NotificationChannel;
import com.supplysense.model.NotificationSeverity;
import com.supplysense.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class AlertNotificationConsumer {

    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    private static final UUID SYSTEM_ORG = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID SYSTEM_USER = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @KafkaListener(topics = "risk.alerts", groupId = "notification-service-alerts")
    public void onRiskAlert(String message) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> event = objectMapper.readValue(message, Map.class);
            String eventType = (String) event.get("event");

            if (!"ALERT_CREATED".equals(eventType)) return;

            String supplierId = (String) event.get("supplierId");
            String orgIdStr   = (String) event.get("orgId");
            String severityStr = (String) event.getOrDefault("severity", "HIGH");

            UUID orgId = orgIdStr != null ? UUID.fromString(orgIdStr) : SYSTEM_ORG;

            NotificationSeverity severity = switch (severityStr) {
                case "CRITICAL" -> NotificationSeverity.CRITICAL;
                case "HIGH"     -> NotificationSeverity.HIGH;
                case "MEDIUM"   -> NotificationSeverity.MEDIUM;
                default         -> NotificationSeverity.LOW;
            };

            // In production: look up org admin users and notify each
            notificationService.sendNotification(
                    SYSTEM_USER, orgId,
                    "Risk Alert: " + severityStr + " risk detected",
                    "A " + severityStr + " risk event has been detected for supplier " + supplierId +
                    ". Please review the Risk Dashboard immediately.",
                    NotificationChannel.IN_APP,
                    severity,
                    supplierId,
                    "SUPPLIER"
            );

            // Also send email for critical alerts
            if (severity == NotificationSeverity.CRITICAL) {
                notificationService.sendNotification(
                        SYSTEM_USER, orgId,
                        "CRITICAL Risk Alert",
                        "Immediate attention required: Critical risk detected for supplier " + supplierId,
                        NotificationChannel.EMAIL,
                        severity,
                        supplierId,
                        "SUPPLIER"
                );
            }

            log.info("Notification dispatched for risk alert: supplier={}, severity={}", supplierId, severityStr);
        } catch (Exception e) {
            log.error("Failed to process risk alert for notification: {}", e.getMessage(), e);
        }
    }
}
