package com.learn.auth.service;

import com.learn.auth.dtos.CreateTenantRequest;
import com.learn.auth.dtos.ResetTenantPasswordRequest;
import com.learn.auth.dtos.TenantResponse;
import com.learn.auth.dtos.UpdateTenantRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TenantService {

    TenantResponse createTenant(CreateTenantRequest request);

    Page<TenantResponse> getAllTenants(String search, Pageable pageable);

    TenantResponse getTenantById(String id);

    TenantResponse updateTenant(String id, UpdateTenantRequest request);

    void deleteTenant(String id);

    void resetTenantAdminPassword(String id, ResetTenantPasswordRequest request);
}
