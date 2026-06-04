package com.supplysense.service;

import com.supplysense.dto.SupplierDto;
import com.supplysense.dto.SupplierDto.*;
import com.supplysense.model.Supplier;
import com.supplysense.model.SupplierStatus;
import com.supplysense.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SupplierService {

    private final SupplierRepository supplierRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String TOPIC_SUPPLIER_EVENTS = "supplier.events";
    private static final String CACHE_SUPPLIERS = "suppliers";

    @Transactional(readOnly = true)
    public Page<SupplierSummaryResponse> getSuppliers(UUID orgId, String search, SupplierStatus status, Pageable pageable) {
        Page<Supplier> suppliers;
        if (search != null && !search.isBlank()) {
            suppliers = supplierRepository.searchByOrgId(orgId, search, pageable);
        } else if (status != null) {
            suppliers = supplierRepository.findByOrgIdAndStatus(orgId, status, pageable);
        } else {
            suppliers = supplierRepository.findByOrgId(orgId, pageable);
        }
        return suppliers.map(this::toSummaryResponse);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = CACHE_SUPPLIERS, key = "#id + ':' + #orgId")
    public SupplierDetailResponse getSupplierById(UUID id, UUID orgId) {
        Supplier supplier = supplierRepository.findByIdAndOrgId(id, orgId)
                .orElseThrow(() -> new NoSuchElementException("Supplier not found: " + id));
        return toDetailResponse(supplier);
    }

    @Transactional
    public SupplierDetailResponse createSupplier(CreateSupplierRequest request, UUID orgId) {
        Supplier supplier = Supplier.builder()
                .name(request.getName())
                .code(request.getCode())
                .description(request.getDescription())
                .country(request.getCountry())
                .region(request.getRegion())
                .city(request.getCity())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .industry(request.getIndustry())
                .category(request.getCategory())
                .contactName(request.getContactName())
                .contactEmail(request.getContactEmail())
                .contactPhone(request.getContactPhone())
                .website(request.getWebsite())
                .orgId(orgId)
                .build();

        supplier = supplierRepository.save(supplier);

        kafkaTemplate.send(TOPIC_SUPPLIER_EVENTS, supplier.getId().toString(),
                Map.of("event", "SUPPLIER_CREATED", "supplierId", supplier.getId().toString(), "orgId", orgId.toString()));

        log.info("Supplier created: {} for org: {}", supplier.getId(), orgId);
        return toDetailResponse(supplier);
    }

    @Transactional
    @CacheEvict(value = CACHE_SUPPLIERS, key = "#id + ':' + #orgId")
    public SupplierDetailResponse updateSupplier(UUID id, UpdateSupplierRequest request, UUID orgId) {
        Supplier supplier = supplierRepository.findByIdAndOrgId(id, orgId)
                .orElseThrow(() -> new NoSuchElementException("Supplier not found: " + id));

        if (request.getName() != null) supplier.setName(request.getName());
        if (request.getDescription() != null) supplier.setDescription(request.getDescription());
        if (request.getCountry() != null) supplier.setCountry(request.getCountry());
        if (request.getRegion() != null) supplier.setRegion(request.getRegion());
        if (request.getCity() != null) supplier.setCity(request.getCity());
        if (request.getLatitude() != null) supplier.setLatitude(request.getLatitude());
        if (request.getLongitude() != null) supplier.setLongitude(request.getLongitude());
        if (request.getStatus() != null) supplier.setStatus(request.getStatus());
        if (request.getContactName() != null) supplier.setContactName(request.getContactName());
        if (request.getContactEmail() != null) supplier.setContactEmail(request.getContactEmail());

        supplier = supplierRepository.save(supplier);
        kafkaTemplate.send(TOPIC_SUPPLIER_EVENTS, supplier.getId().toString(),
                Map.of("event", "SUPPLIER_UPDATED", "supplierId", supplier.getId().toString()));

        return toDetailResponse(supplier);
    }

    @Transactional
    @CacheEvict(value = CACHE_SUPPLIERS, key = "#id + ':' + #orgId")
    public void deleteSupplier(UUID id, UUID orgId) {
        Supplier supplier = supplierRepository.findByIdAndOrgId(id, orgId)
                .orElseThrow(() -> new NoSuchElementException("Supplier not found: " + id));
        supplier.setStatus(SupplierStatus.INACTIVE);
        supplierRepository.save(supplier);
        kafkaTemplate.send(TOPIC_SUPPLIER_EVENTS, id.toString(),
                Map.of("event", "SUPPLIER_DEACTIVATED", "supplierId", id.toString()));
    }

    public SupplierStatsResponse getStats(UUID orgId) {
        long total = supplierRepository.countByOrgIdAndStatus(orgId, SupplierStatus.ACTIVE);
        long inactive = supplierRepository.countByOrgIdAndStatus(orgId, SupplierStatus.INACTIVE);
        long probation = supplierRepository.countByOrgIdAndStatus(orgId, SupplierStatus.PROBATION);

        return SupplierStatsResponse.builder()
                .totalActive(total)
                .totalInactive(inactive)
                .onProbation(probation)
                .countries(supplierRepository.findDistinctCountriesByOrgId(orgId))
                .build();
    }

    private SupplierSummaryResponse toSummaryResponse(Supplier s) {
        return SupplierSummaryResponse.builder()
                .id(s.getId())
                .name(s.getName())
                .code(s.getCode())
                .country(s.getCountry())
                .city(s.getCity())
                .industry(s.getIndustry())
                .status(s.getStatus())
                .reliabilityScore(s.getReliabilityScore())
                .qualityScore(s.getQualityScore())
                .deliveryScore(s.getDeliveryScore())
                .latitude(s.getLatitude())
                .longitude(s.getLongitude())
                .build();
    }

    private SupplierDetailResponse toDetailResponse(Supplier s) {
        return SupplierDetailResponse.builder()
                .id(s.getId())
                .name(s.getName())
                .code(s.getCode())
                .description(s.getDescription())
                .country(s.getCountry())
                .region(s.getRegion())
                .city(s.getCity())
                .latitude(s.getLatitude())
                .longitude(s.getLongitude())
                .industry(s.getIndustry())
                .category(s.getCategory())
                .status(s.getStatus())
                .reliabilityScore(s.getReliabilityScore())
                .qualityScore(s.getQualityScore())
                .deliveryScore(s.getDeliveryScore())
                .annualSpend(s.getAnnualSpend())
                .contactName(s.getContactName())
                .contactEmail(s.getContactEmail())
                .contactPhone(s.getContactPhone())
                .website(s.getWebsite())
                .createdAt(s.getCreatedAt())
                .updatedAt(s.getUpdatedAt())
                .build();
    }
}
