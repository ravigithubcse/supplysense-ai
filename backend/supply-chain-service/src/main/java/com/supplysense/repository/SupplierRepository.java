package com.supplysense.repository;

import com.supplysense.model.Supplier;
import com.supplysense.model.SupplierStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, UUID> {

    Page<Supplier> findByOrgId(UUID orgId, Pageable pageable);

    Page<Supplier> findByOrgIdAndStatus(UUID orgId, SupplierStatus status, Pageable pageable);

    Optional<Supplier> findByIdAndOrgId(UUID id, UUID orgId);

    @Query("SELECT s FROM Supplier s WHERE s.orgId = :orgId " +
           "AND (LOWER(s.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(s.code) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(s.country) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Supplier> searchByOrgId(@Param("orgId") UUID orgId,
                                  @Param("search") String search,
                                  Pageable pageable);

    @Query("SELECT s FROM Supplier s WHERE s.orgId = :orgId AND s.country = :country")
    List<Supplier> findByOrgIdAndCountry(@Param("orgId") UUID orgId,
                                          @Param("country") String country);

    long countByOrgIdAndStatus(UUID orgId, SupplierStatus status);

    @Query("SELECT DISTINCT s.country FROM Supplier s WHERE s.orgId = :orgId ORDER BY s.country")
    List<String> findDistinctCountriesByOrgId(@Param("orgId") UUID orgId);
}
