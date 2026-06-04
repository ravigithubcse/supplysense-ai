package com.supplysense.controller;

import com.supplysense.dto.SupplierDto.*;
import com.supplysense.model.SupplierStatus;
import com.supplysense.service.SupplierService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/suppliers")
@RequiredArgsConstructor
@Tag(name = "Suppliers", description = "Supplier management endpoints")
public class SupplierController {

    private final SupplierService supplierService;

    @GetMapping
    @Operation(summary = "List suppliers with pagination and filtering")
    public ResponseEntity<Page<SupplierSummaryResponse>> getSuppliers(
            @RequestHeader("X-Org-ID") UUID orgId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) SupplierStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {

        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        PageRequest pageable = PageRequest.of(page, size, sort);

        return ResponseEntity.ok(supplierService.getSuppliers(orgId, search, status, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get supplier details by ID")
    public ResponseEntity<SupplierDetailResponse> getSupplier(
            @PathVariable UUID id,
            @RequestHeader("X-Org-ID") UUID orgId) {
        return ResponseEntity.ok(supplierService.getSupplierById(id, orgId));
    }

    @PostMapping
    @Operation(summary = "Create a new supplier")
    public ResponseEntity<SupplierDetailResponse> createSupplier(
            @Valid @RequestBody CreateSupplierRequest request,
            @RequestHeader("X-Org-ID") UUID orgId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(supplierService.createSupplier(request, orgId));
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Update an existing supplier")
    public ResponseEntity<SupplierDetailResponse> updateSupplier(
            @PathVariable UUID id,
            @RequestBody UpdateSupplierRequest request,
            @RequestHeader("X-Org-ID") UUID orgId) {
        return ResponseEntity.ok(supplierService.updateSupplier(id, request, orgId));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deactivate a supplier")
    public ResponseEntity<Void> deleteSupplier(
            @PathVariable UUID id,
            @RequestHeader("X-Org-ID") UUID orgId) {
        supplierService.deleteSupplier(id, orgId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/stats")
    @Operation(summary = "Get supplier statistics for the organization")
    public ResponseEntity<SupplierStatsResponse> getStats(
            @RequestHeader("X-Org-ID") UUID orgId) {
        return ResponseEntity.ok(supplierService.getStats(orgId));
    }
}
