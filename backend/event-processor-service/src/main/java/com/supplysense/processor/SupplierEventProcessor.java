package com.supplysense.processor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class SupplierEventProcessor {

    private final SimpMessagingTemplate messagingTemplate;

    public void processSupplierEvent(Map<String, Object> event) {
        String eventType  = (String) event.get("event");
        String supplierId = (String) event.get("supplierId");
        String orgId      = (String) event.get("orgId");

        log.info("Processing supplier event: type={}, supplier={}", eventType, supplierId);

        messagingTemplate.convertAndSend(
                "/topic/suppliers" + (orgId != null ? "/" + orgId : ""),
                Map.of(
                    "type",       eventType,
                    "supplierId", supplierId != null ? supplierId : "",
                    "timestamp",  System.currentTimeMillis()
                )
        );
    }
}
