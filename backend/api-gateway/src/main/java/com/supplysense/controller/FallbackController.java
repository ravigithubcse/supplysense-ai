package com.supplysense.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @RequestMapping("/supply-chain")
    public ResponseEntity<Map<String, Object>> supplyChainFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "error", "Supply Chain Service is temporarily unavailable",
                        "message", "Please try again in a few moments",
                        "status", 503,
                        "timestamp", Instant.now().toString()
                ));
    }

    @RequestMapping("/risk")
    public ResponseEntity<Map<String, Object>> riskFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "error", "Risk Engine Service is temporarily unavailable",
                        "message", "Risk assessment is temporarily unavailable. Last known risk scores are still displayed.",
                        "status", 503,
                        "timestamp", Instant.now().toString()
                ));
    }

    @RequestMapping("/default")
    public ResponseEntity<Map<String, Object>> defaultFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "error", "Service temporarily unavailable",
                        "message", "The service is temporarily unavailable. Please try again.",
                        "status", 503,
                        "timestamp", Instant.now().toString()
                ));
    }
}
