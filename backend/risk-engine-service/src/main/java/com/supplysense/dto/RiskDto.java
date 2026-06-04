package com.supplysense.dto;

import com.supplysense.model.AlertSeverity;
import com.supplysense.model.AlertStatus;
import com.supplysense.model.AlertType;
import com.supplysense.model.RiskLevel;
import lombok.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class RiskDto {

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class RiskScoreSummary {
        private UUID supplierId;
        private Double compositeScore;
        private RiskLevel riskLevel;
        private Double forecastScore7d;
        private Double forecastScore30d;
        private Instant calculatedAt;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class RiskScoreDetail {
        private UUID id;
        private UUID supplierId;
        private Double compositeScore;
        private Double geopoliticalScore;
        private Double weatherScore;
        private Double financialScore;
        private Double logisticsScore;
        private Double sentimentScore;
        private Double forecastScore7d;
        private Double forecastScore30d;
        private Double confidence;
        private RiskLevel riskLevel;
        private String modelVersion;
        private Instant calculatedAt;
        private List<HistoryPoint> history;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class HistoryPoint {
        private Double score;
        private RiskLevel riskLevel;
        private Instant timestamp;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class RiskDashboardStats {
        private int totalSuppliers;
        private int criticalRiskCount;
        private int highRiskCount;
        private double averageRiskScore;
        private long activeAlerts;
        private long criticalAlerts;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class RiskAlertResponse {
        private UUID id;
        private UUID supplierId;
        private String title;
        private String description;
        private AlertSeverity severity;
        private AlertType type;
        private AlertStatus status;
        private String affectedCountry;
        private Instant createdAt;
        private Instant resolvedAt;
        private String resolution;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ResolveAlertRequest {
        private String resolution;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class CalculateRiskRequest {
        private UUID supplierId;
        private String country;
        private String industry;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class WhatIfScenarioRequest {
        private UUID supplierId;
        private String scenarioType;       // e.g. "port_closure", "tariff_increase"
        private Double impactFactor;       // 0.0 - 1.0
        private String description;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class WhatIfScenarioResponse {
        private UUID supplierId;
        private String scenarioType;
        private Double baselineScore;
        private Double projectedScore;
        private Double scoreDelta;
        private RiskLevel projectedRiskLevel;
        private List<String> recommendedMitigations;
    }
}
