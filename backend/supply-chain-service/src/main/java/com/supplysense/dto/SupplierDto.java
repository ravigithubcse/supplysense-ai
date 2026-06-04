package com.supplysense.dto;

import com.supplysense.model.SupplierStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class SupplierDto {

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class CreateSupplierRequest {
        @NotBlank private String name;
        @NotBlank @Size(max = 50) private String code;
        private String description;
        @NotBlank private String country;
        private String region;
        private String city;
        private BigDecimal latitude;
        private BigDecimal longitude;
        private String industry;
        private String category;
        private String contactName;
        private String contactEmail;
        private String contactPhone;
        private String website;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class UpdateSupplierRequest {
        private String name;
        private String description;
        private String country;
        private String region;
        private String city;
        private BigDecimal latitude;
        private BigDecimal longitude;
        private SupplierStatus status;
        private String contactName;
        private String contactEmail;
        private String contactPhone;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class SupplierSummaryResponse {
        private UUID id;
        private String name;
        private String code;
        private String country;
        private String city;
        private String industry;
        private SupplierStatus status;
        private Integer reliabilityScore;
        private Integer qualityScore;
        private Integer deliveryScore;
        private BigDecimal latitude;
        private BigDecimal longitude;
        private Double riskScore;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class SupplierDetailResponse {
        private UUID id;
        private String name;
        private String code;
        private String description;
        private String country;
        private String region;
        private String city;
        private BigDecimal latitude;
        private BigDecimal longitude;
        private String industry;
        private String category;
        private SupplierStatus status;
        private Integer reliabilityScore;
        private Integer qualityScore;
        private Integer deliveryScore;
        private BigDecimal annualSpend;
        private String contactName;
        private String contactEmail;
        private String contactPhone;
        private String website;
        private Instant createdAt;
        private Instant updatedAt;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class SupplierStatsResponse {
        private long totalActive;
        private long totalInactive;
        private long onProbation;
        private List<String> countries;
    }
}
