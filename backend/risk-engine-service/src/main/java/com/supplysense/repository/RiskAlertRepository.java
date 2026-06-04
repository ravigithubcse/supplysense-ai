package com.supplysense.repository;

import com.supplysense.model.AlertSeverity;
import com.supplysense.model.AlertStatus;
import com.supplysense.model.RiskAlert;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RiskAlertRepository extends JpaRepository<RiskAlert, UUID> {

    Page<RiskAlert> findByOrgIdOrderByCreatedAtDesc(UUID orgId, Pageable pageable);

    Page<RiskAlert> findByOrgIdAndStatusOrderByCreatedAtDesc(UUID orgId, AlertStatus status, Pageable pageable);

    Page<RiskAlert> findByOrgIdAndSeverityOrderByCreatedAtDesc(UUID orgId, AlertSeverity severity, Pageable pageable);

    List<RiskAlert> findBySupplierIdAndOrgIdAndStatusOrderByCreatedAtDesc(UUID supplierId, UUID orgId, AlertStatus status);

    Optional<RiskAlert> findByIdAndOrgId(UUID id, UUID orgId);

    long countByOrgIdAndStatus(UUID orgId, AlertStatus status);

    long countByOrgIdAndSeverityAndStatus(UUID orgId, AlertSeverity severity, AlertStatus status);
}
