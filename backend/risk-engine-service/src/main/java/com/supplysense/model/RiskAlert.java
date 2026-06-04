package com.supplysense.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "risk_alerts", schema = "risk")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RiskAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID supplierId;

    @Column(nullable = false)
    private UUID orgId;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AlertSeverity severity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AlertType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private AlertStatus status = AlertStatus.ACTIVE;

    private Double riskScoreDelta;
    private String affectedCountry;
    private String affectedRegion;

    /** JSON: recommended actions */
    @Column(columnDefinition = "TEXT")
    private String recommendationsJson;

    private String resolvedBy;
    private Instant resolvedAt;
    private String resolution;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    private Instant expiresAt;
}
