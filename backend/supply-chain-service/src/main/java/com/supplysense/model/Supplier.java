package com.supplysense.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "suppliers", schema = "supply_chain")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Supplier {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String code;

    private String description;

    @Column(nullable = false)
    private String country;

    private String region;
    private String city;

    @Column(precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(precision = 10, scale = 7)
    private BigDecimal longitude;

    private String industry;
    private String category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private SupplierStatus status = SupplierStatus.ACTIVE;

    @Column(nullable = false)
    @Builder.Default
    private Integer reliabilityScore = 75;

    @Column(nullable = false)
    @Builder.Default
    private Integer qualityScore = 75;

    @Column(nullable = false)
    @Builder.Default
    private Integer deliveryScore = 75;

    @Column(nullable = false)
    @Builder.Default
    private BigDecimal annualSpend = BigDecimal.ZERO;

    private String contactName;
    private String contactEmail;
    private String contactPhone;
    private String website;

    @Column(nullable = false)
    private UUID orgId;

    @OneToMany(mappedBy = "supplier", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<SupplierProduct> products = new ArrayList<>();

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;
}
