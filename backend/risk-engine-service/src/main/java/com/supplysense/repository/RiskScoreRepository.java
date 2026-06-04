package com.supplysense.repository;

import com.supplysense.model.RiskScore;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RiskScoreRepository extends JpaRepository<RiskScore, UUID> {

    /** Latest risk score per supplier */
    @Query(value = """
        SELECT DISTINCT ON (supplier_id) *
        FROM risk.risk_scores
        WHERE org_id = :orgId
        ORDER BY supplier_id, calculated_at DESC
        """, nativeQuery = true)
    List<RiskScore> findLatestByOrgId(@Param("orgId") UUID orgId);

    /** Latest score for one supplier */
    Optional<RiskScore> findTopBySupplierIdAndOrgIdOrderByCalculatedAtDesc(UUID supplierId, UUID orgId);

    /** Time series for a supplier */
    List<RiskScore> findBySupplierIdAndOrgIdAndCalculatedAtBetweenOrderByCalculatedAtAsc(
            UUID supplierId, UUID orgId, Instant from, Instant to);

    /** Suppliers above risk threshold */
    @Query("""
        SELECT r FROM RiskScore r
        WHERE r.orgId = :orgId
          AND r.compositeScore >= :threshold
          AND r.calculatedAt = (
              SELECT MAX(r2.calculatedAt) FROM RiskScore r2
              WHERE r2.supplierId = r.supplierId
          )
        ORDER BY r.compositeScore DESC
        """)
    Page<RiskScore> findHighRiskSuppliers(@Param("orgId") UUID orgId,
                                           @Param("threshold") Double threshold,
                                           Pageable pageable);

    @Query(value = """
        SELECT AVG(rs.composite_score)
        FROM (
          SELECT DISTINCT ON (supplier_id) composite_score
          FROM risk.risk_scores
          WHERE org_id = :orgId
          ORDER BY supplier_id, calculated_at DESC
        ) rs
        """, nativeQuery = true)
    Double findAverageRiskScore(@Param("orgId") UUID orgId);
}
