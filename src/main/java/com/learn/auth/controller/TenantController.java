package com.learn.auth.controller;

import com.learn.auth.dtos.*;
import com.learn.auth.service.TenantService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tenants")
public class TenantController {

    private final TenantService tenantService;

    public TenantController(TenantService tenantService) {
        this.tenantService = tenantService;
    }

    @PreAuthorize("hasAuthority('tenant.create')")
    @PostMapping
    public ResponseEntity<ApiResponse<TenantResponse>> createTenant(
            @RequestBody @Valid CreateTenantRequest request) {
        TenantResponse response = tenantService.createTenant(request);
        return new ResponseEntity<>(ApiResponse.success("Tenant created successfully", response), HttpStatus.CREATED);
    }

    @PreAuthorize("hasAuthority('tenant.view')")
    @GetMapping
    public ResponseEntity<ApiResponse<List<TenantResponse>>> getAllTenants(
            @RequestParam(required = false) String search,
            @PageableDefault(page = 0, size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<TenantResponse> tenants = tenantService.getAllTenants(search, pageable);
        return ResponseEntity.ok(ApiResponse.success("Tenants fetched successfully", tenants));
    }

    @PreAuthorize("hasAuthority('tenant.view')")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TenantResponse>> getTenantById(@PathVariable String id) {
        TenantResponse tenant = tenantService.getTenantById(id);
        return ResponseEntity.ok(ApiResponse.success("Tenant fetched successfully", tenant));
    }

    @PreAuthorize("hasAuthority('tenant.update')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TenantResponse>> updateTenant(
            @PathVariable String id,
            @RequestBody @Valid UpdateTenantRequest request) {
        TenantResponse updated = tenantService.updateTenant(id, request);
        return ResponseEntity.ok(ApiResponse.success("Tenant updated successfully", updated));
    }

    @PreAuthorize("hasAuthority('tenant.delete')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTenant(@PathVariable String id) {
        tenantService.deleteTenant(id);
        return ResponseEntity.ok(ApiResponse.success("Tenant deleted successfully"));
    }

    @PreAuthorize("hasAuthority('tenant.password_reset')")
    @PostMapping("/{id}/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetTenantPassword(
            @PathVariable String id,
            @RequestBody @Valid ResetTenantPasswordRequest request) {
        tenantService.resetTenantAdminPassword(id, request);
        return ResponseEntity.ok(ApiResponse.success("Tenant admin password reset successfully"));
    }
}
