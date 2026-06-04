package com.supplysense.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.supplysense.client.AiServiceClient;
import com.supplysense.client.AiServiceClient.*;
import com.supplysense.dto.RiskDto.*;
import com.supplysense.model.*;
import com.supplysense.repository.RiskAlertRepository;
import com.supplysense.repository.RiskScoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class RiskService {

    private final RiskScoreRepository riskScoreRepository;
    private final RiskAlertRepository riskAlertRepository;
    private final AiServiceClient aiServiceClient;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    private static final String TOPIC_RISK_UPDATES = "risk.score.updates";
    private static final String TOPIC_ALERTS       = "risk.alerts";
    private static final double CRITICAL_THRESHOLD = 80.0;
    private static final double HIGH_THRESHOLD     = 60.0;

    // ──────────────────────────────────────────
    // Score retrieval
    // ──────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<RiskScoreSummary> getLatestScores(UUID orgId) {
        return riskScoreRepository.findLatestByOrgId(orgId)
                .stream()
                .map(this::toSummary)
                .toList();
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "risk-scores", key = "#supplierId + ':' + #orgId")
    public RiskScoreDetail getSupplierRiskScore(UUID supplierId, UUID orgId) {
        RiskScore latest = riskScoreRepository
                .findTopBySupplierIdAndOrgIdOrderByCalculatedAtDesc(supplierId, orgId)
                .orElseThrow(() -> new NoSuchElementException("No risk score for supplier: " + supplierId));

        Instant from = Instant.now().minus(30, ChronoUnit.DAYS);
        List<RiskScore> history = riskScoreRepository
                .findBySupplierIdAndOrgIdAndCalculatedAtBetweenOrderByCalculatedAtAsc(
                        supplierId, orgId, from, Instant.now());

        return toDetail(latest, history);
    }

    @Transactional(readOnly = true)
    public RiskDashboardStats getDashboardStats(UUID orgId) {
        List<RiskScore> latest = riskScoreRepository.findLatestByOrgId(orgId);
        long critical = latest.stream().filter(r -> r.getCompositeScore() >= CRITICAL_THRESHOLD).count();
        long high     = latest.stream().filter(r -> r.getCompositeScore() >= HIGH_THRESHOLD && r.getCompositeScore() < CRITICAL_THRESHOLD).count();
        Double avg    = riskScoreRepository.findAverageRiskScore(orgId);
        long activeAlerts = riskAlertRepository.countByOrgIdAndStatus(orgId, AlertStatus.ACTIVE);
        long criticalAlerts = riskAlertRepository.countByOrgIdAndSeverityAndStatus(orgId, AlertSeverity.CRITICAL, AlertStatus.ACTIVE);

        return RiskDashboardStats.builder()
                .totalSuppliers(latest.size())
                .criticalRiskCount((int) critical)
                .highRiskCount((int) high)
                .averageRiskScore(avg != null ? avg : 0.0)
                .activeAlerts(activeAlerts)
                .criticalAlerts(criticalAlerts)
                .build();
    }

    // ──────────────────────────────────────────
    // Risk calculation
    // ──────────────────────────────────────────

    @Transactional
    public RiskScoreDetail calculateRisk(UUID supplierId, UUID orgId, String country, String industry) {
        log.info("Calculating risk for supplier: {} in org: {}", supplierId, orgId);

        RiskPredictionRequest req = RiskPredictionRequest.builder()
                .supplierId(supplierId.toString())
                .country(country)
                .industry(industry)
                .historicalMetrics(Collections.emptyMap())
                .build();

        RiskPredictionResponse prediction = aiServiceClient.predictRisk(req).block();
        if (prediction == null) {
            throw new IllegalStateException("AI service returned null prediction");
        }

        RiskScore score = RiskScore.builder()
                .supplierId(supplierId)
                .orgId(orgId)
                .compositeScore(prediction.getCompositeScore())
                .geopoliticalScore(prediction.getGeopoliticalScore())
                .weatherScore(prediction.getWeatherScore())
                .financialScore(prediction.getFinancialScore())
                .logisticsScore(prediction.getLogisticsScore())
                .sentimentScore(prediction.getSentimentScore())
                .forecastScore7d(prediction.getForecastScore7d())
                .forecastScore30d(prediction.getForecastScore30d())
                .confidence(prediction.getConfidence())
                .riskLevel(classifyRisk(prediction.getCompositeScore()))
                .modelVersion(prediction.getModelVersion())
                .factorsJson(toJson(prediction.getFactors()))
                .mitigationsJson(toJson(prediction.getMitigations()))
                .build();

        score = riskScoreRepository.save(score);

        // Emit Kafka event
        kafkaTemplate.send(TOPIC_RISK_UPDATES, supplierId.toString(),
                Map.of("supplierId", supplierId, "score", score.getCompositeScore(),
                        "riskLevel", score.getRiskLevel(), "orgId", orgId));

        // Auto-create alert if high/critical
        if (score.getCompositeScore() >= HIGH_THRESHOLD) {
            createRiskAlert(score, orgId, country);
        }

        return toDetail(score, Collections.emptyList());
    }

    // ──────────────────────────────────────────
    // Alerts
    // ──────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<RiskAlertResponse> getAlerts(UUID orgId, AlertStatus status, AlertSeverity severity, Pageable pageable) {
        Page<RiskAlert> alerts;
        if (status != null) {
            alerts = riskAlertRepository.findByOrgIdAndStatusOrderByCreatedAtDesc(orgId, status, pageable);
        } else if (severity != null) {
            alerts = riskAlertRepository.findByOrgIdAndSeverityOrderByCreatedAtDesc(orgId, severity, pageable);
        } else {
            alerts = riskAlertRepository.findByOrgIdOrderByCreatedAtDesc(orgId, pageable);
        }
        return alerts.map(this::toAlertResponse);
    }

    @Transactional
    public RiskAlertResponse acknowledgeAlert(UUID alertId, UUID orgId, String userId) {
        RiskAlert alert = riskAlertRepository.findByIdAndOrgId(alertId, orgId)
                .orElseThrow(() -> new NoSuchElementException("Alert not found: " + alertId));
        alert.setStatus(AlertStatus.ACKNOWLEDGED);
        alert = riskAlertRepository.save(alert);
        log.info("Alert {} acknowledged by {}", alertId, userId);
        return toAlertResponse(alert);
    }

    @Transactional
    public RiskAlertResponse resolveAlert(UUID alertId, UUID orgId, String userId, String resolution) {
        RiskAlert alert = riskAlertRepository.findByIdAndOrgId(alertId, orgId)
                .orElseThrow(() -> new NoSuchElementException("Alert not found: " + alertId));
        alert.setStatus(AlertStatus.RESOLVED);
        alert.setResolvedBy(userId);
        alert.setResolvedAt(Instant.now());
        alert.setResolution(resolution);
        alert = riskAlertRepository.save(alert);

        kafkaTemplate.send(TOPIC_ALERTS, alertId.toString(),
                Map.of("event", "ALERT_RESOLVED", "alertId", alertId, "orgId", orgId));

        return toAlertResponse(alert);
    }

    // ──────────────────────────────────────────
    // Scheduled batch recalculation
    // ──────────────────────────────────────────

    @Scheduled(fixedDelayString = "${risk.recalculation.interval:300000}")
    public void scheduledRiskRecalculation() {
        log.debug("Scheduled risk recalculation triggered");
        // In production: fetch active suppliers per org and enqueue calculation tasks
        kafkaTemplate.send("risk.recalculation.trigger",
                Map.of("trigger", "scheduled", "timestamp", Instant.now().toString()));
    }

    // ──────────────────────────────────────────
    // Private helpers
    // ──────────────────────────────────────────

    private void createRiskAlert(RiskScore score, UUID orgId, String country) {
        AlertSeverity severity = score.getCompositeScore() >= CRITICAL_THRESHOLD
                ? AlertSeverity.CRITICAL : AlertSeverity.HIGH;

        RiskAlert alert = RiskAlert.builder()
                .supplierId(score.getSupplierId())
                .orgId(orgId)
                .title(String.format("%s risk detected for supplier", severity.name()))
                .description(String.format(
                        "Composite risk score of %.1f detected (Level: %s). Immediate review recommended.",
                        score.getCompositeScore(), score.getRiskLevel()))
                .severity(severity)
                .type(AlertType.FORECAST)
                .riskScoreDelta(score.getCompositeScore())
                .affectedCountry(country)
                .expiresAt(Instant.now().plus(7, ChronoUnit.DAYS))
                .build();

        riskAlertRepository.save(alert);
        kafkaTemplate.send(TOPIC_ALERTS, score.getSupplierId().toString(),
                Map.of("event", "ALERT_CREATED", "supplierId", score.getSupplierId(),
                        "severity", severity, "orgId", orgId));
    }

    private RiskLevel classifyRisk(Double score) {
        if (score >= 80) return RiskLevel.CRITICAL;
        if (score >= 60) return RiskLevel.HIGH;
        if (score >= 40) return RiskLevel.MEDIUM;
        if (score >= 20) return RiskLevel.LOW;
        return RiskLevel.MINIMAL;
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return "{}";
        }
    }

    private RiskScoreSummary toSummary(RiskScore s) {
        return RiskScoreSummary.builder()
                .supplierId(s.getSupplierId())
                .compositeScore(s.getCompositeScore())
                .riskLevel(s.getRiskLevel())
                .forecastScore7d(s.getForecastScore7d())
                .forecastScore30d(s.getForecastScore30d())
                .calculatedAt(s.getCalculatedAt())
                .build();
    }

    private RiskScoreDetail toDetail(RiskScore s, List<RiskScore> history) {
        return RiskScoreDetail.builder()
                .id(s.getId())
                .supplierId(s.getSupplierId())
                .compositeScore(s.getCompositeScore())
                .geopoliticalScore(s.getGeopoliticalScore())
                .weatherScore(s.getWeatherScore())
                .financialScore(s.getFinancialScore())
                .logisticsScore(s.getLogisticsScore())
                .sentimentScore(s.getSentimentScore())
                .forecastScore7d(s.getForecastScore7d())
                .forecastScore30d(s.getForecastScore30d())
                .confidence(s.getConfidence())
                .riskLevel(s.getRiskLevel())
                .modelVersion(s.getModelVersion())
                .calculatedAt(s.getCalculatedAt())
                .history(history.stream().map(h -> HistoryPoint.builder()
                        .score(h.getCompositeScore())
                        .riskLevel(h.getRiskLevel())
                        .timestamp(h.getCalculatedAt())
                        .build()).toList())
                .build();
    }

    private RiskAlertResponse toAlertResponse(RiskAlert a) {
        return RiskAlertResponse.builder()
                .id(a.getId())
                .supplierId(a.getSupplierId())
                .title(a.getTitle())
                .description(a.getDescription())
                .severity(a.getSeverity())
                .type(a.getType())
                .status(a.getStatus())
                .affectedCountry(a.getAffectedCountry())
                .createdAt(a.getCreatedAt())
                .resolvedAt(a.getResolvedAt())
                .build();
    }

    public com.supplysense.dto.RiskDto.WhatIfScenarioResponse runWhatIfScenario(
            com.supplysense.dto.RiskDto.WhatIfScenarioRequest request, UUID orgId) {
        RiskScore baseline = riskScoreRepository
                .findTopBySupplierIdAndOrgIdOrderByCalculatedAtDesc(request.getSupplierId(), orgId)
                .orElseThrow(() -> new NoSuchElementException("No baseline score for supplier: " + request.getSupplierId()));

        double baseScore = baseline.getCompositeScore();
        double multiplier = switch (request.getScenarioType()) {
            case "port_closure"      -> 1.4;
            case "tariff_increase"   -> 1.2;
            case "natural_disaster"  -> 1.6;
            case "political_unrest"  -> 1.35;
            case "supplier_bankrupt" -> 1.8;
            default -> 1.0 + (request.getImpactFactor() != null ? request.getImpactFactor() : 0.2);
        };

        double projectedScore = Math.min(100.0,
                baseScore + (baseScore * (multiplier - 1.0) * (request.getImpactFactor() != null ? request.getImpactFactor() : 0.5)));

        return com.supplysense.dto.RiskDto.WhatIfScenarioResponse.builder()
                .supplierId(request.getSupplierId())
                .scenarioType(request.getScenarioType())
                .baselineScore(baseScore)
                .projectedScore(projectedScore)
                .scoreDelta(projectedScore - baseScore)
                .projectedRiskLevel(classifyRisk(projectedScore))
                .recommendedMitigations(List.of(
                        "Identify alternative suppliers in different regions",
                        "Increase safety stock for critical components",
                        "Activate contingency logistics partners",
                        "Review and activate force-majeure contract clauses",
                        "Engage freight forwarders for alternate routing"))
                .build();
    }

}