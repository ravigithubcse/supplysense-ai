package com.supplysense.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "risk_scores", schema = "risk")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RiskScore {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID supplierId;

    @Column(nullable = false)
    private UUID orgId;

    /** Composite 0-100 risk score */
    @Column(nullable = false)
    private Double compositeScore;

    /** Individual factor scores */
    private Double geopoliticalScore;
    private Double weatherScore;
    private Double financialScore;
    private Double logisticsScore;
    private Double sentimentScore;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RiskLevel riskLevel;

    /** ML model confidence 0-1 */
    private Double confidence;

    /** 7-day forecast score */
    private Double forecastScore7d;

    /** 30-day forecast score */
    private Double forecastScore30d;

    /** JSON blob with contributing factors */
    @Column(columnDefinition = "TEXT")
    private String factorsJson;

    /** JSON blob with recommended mitigations */
    @Column(columnDefinition = "TEXT")
    private String mitigationsJson;

    private String modelVersion;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant calculatedAt;
}
