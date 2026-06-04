package com.supplysense.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "supplier_products", schema = "supply_chain")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SupplierProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;

    @Column(nullable = false)
    private String productName;

    @Column(nullable = false)
    private String sku;

    private String category;

    @Column(precision = 15, scale = 2)
    private BigDecimal unitPrice;

    private Integer leadTimeDays;
    private Integer minOrderQuantity;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;
}
