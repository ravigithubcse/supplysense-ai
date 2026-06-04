package com.supplysense.controller;

import com.supplysense.dto.RiskDto.*;
import com.supplysense.model.AlertSeverity;
import com.supplysense.model.AlertStatus;
import com.supplysense.service.RiskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/risk")
@RequiredArgsConstructor
@Tag(name = "Risk Engine", description = "Risk scoring and alert management")
public class RiskController {

    private final RiskService riskService;

    // ── Risk Scores ────────────────────────────────────────────

    @GetMapping("/scores")
    @Operation(summary = "Get latest risk scores for all suppliers in the org")
    public ResponseEntity<List<RiskScoreSummary>> getLatestScores(
            @RequestHeader("X-Org-ID") UUID orgId) {
        return ResponseEntity.ok(riskService.getLatestScores(orgId));
    }

    @GetMapping("/scores/{supplierId}")
    @Operation(summary = "Get detailed risk score for a specific supplier")
    public ResponseEntity<RiskScoreDetail> getSupplierScore(
            @PathVariable UUID supplierId,
            @RequestHeader("X-Org-ID") UUID orgId) {
        return ResponseEntity.ok(riskService.getSupplierRiskScore(supplierId, orgId));
    }

    @PostMapping("/scores/calculate")
    @Operation(summary = "Trigger on-demand risk calculation for a supplier")
    public ResponseEntity<RiskScoreDetail> calculateRisk(
            @Valid @RequestBody CalculateRiskRequest request,
            @RequestHeader("X-Org-ID") UUID orgId) {
        return ResponseEntity.ok(riskService.calculateRisk(
                request.getSupplierId(), orgId, request.getCountry(), request.getIndustry()));
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Get aggregated risk dashboard statistics")
    public ResponseEntity<RiskDashboardStats> getDashboardStats(
            @RequestHeader("X-Org-ID") UUID orgId) {
        return ResponseEntity.ok(riskService.getDashboardStats(orgId));
    }

    // ── Alerts ─────────────────────────────────────────────────

    @GetMapping("/alerts")
    @Operation(summary = "List risk alerts with optional status/severity filters")
    public ResponseEntity<Page<RiskAlertResponse>> getAlerts(
            @RequestHeader("X-Org-ID") UUID orgId,
            @RequestParam(required = false) AlertStatus status,
            @RequestParam(required = false) AlertSeverity severity,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(riskService.getAlerts(orgId, status, severity, pageable));
    }

    @PatchMapping("/alerts/{alertId}/acknowledge")
    @Operation(summary = "Acknowledge a risk alert")
    public ResponseEntity<RiskAlertResponse> acknowledgeAlert(
            @PathVariable UUID alertId,
            @RequestHeader("X-Org-ID") UUID orgId,
            @RequestHeader("X-User-ID") String userId) {
        return ResponseEntity.ok(riskService.acknowledgeAlert(alertId, orgId, userId));
    }

    @PatchMapping("/alerts/{alertId}/resolve")
    @Operation(summary = "Resolve a risk alert with a resolution note")
    public ResponseEntity<RiskAlertResponse> resolveAlert(
            @PathVariable UUID alertId,
            @RequestHeader("X-Org-ID") UUID orgId,
            @RequestHeader("X-User-ID") String userId,
            @RequestBody ResolveAlertRequest request) {
        return ResponseEntity.ok(riskService.resolveAlert(alertId, orgId, userId, request.getResolution()));
    }

    // ── What-If Scenarios ──────────────────────────────────────

    @PostMapping("/scenarios/what-if")
    @Operation(summary = "Run a what-if scenario simulation")
    public ResponseEntity<WhatIfScenarioResponse> runWhatIfScenario(
            @Valid @RequestBody WhatIfScenarioRequest request,
            @RequestHeader("X-Org-ID") UUID orgId) {
        return ResponseEntity.ok(riskService.runWhatIfScenario(request, orgId));
    }
}
