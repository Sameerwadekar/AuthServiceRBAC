package com.learn.auth.service;

import com.learn.auth.dtos.*;
import com.learn.auth.entities.Department;
import com.learn.auth.entities.Status;
import com.learn.auth.exception.ResourceNotFoundException;
import com.learn.auth.repositary.DepartmentRepository;
import com.learn.auth.repositary.TenantRepository;
import com.learn.auth.security.TenantContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final TenantRepository tenantRepository;

    public DepartmentServiceImpl(DepartmentRepository departmentRepository, TenantRepository tenantRepository) {
        this.departmentRepository = departmentRepository;
        this.tenantRepository = tenantRepository;
    }

    private String requireTenantContext() {
        String tenantId = TenantContext.getTenantId();
        if (tenantId == null || tenantId.isBlank()) {
            throw new AccessDeniedException("Access denied: Department management requires a valid tenant workspace context.");
        }
        return tenantId.trim();
    }

    @Override
    @Transactional
    public DepartmentResponse createDepartment(CreateDepartmentRequest request) {
        String tenantId = requireTenantContext();

        if (!tenantRepository.existsById(tenantId)) {
            throw new ResourceNotFoundException("Tenant workspace not found with id: " + tenantId);
        }

        String deptName = request.getName().trim();
        if (departmentRepository.existsByNameIgnoreCaseAndTenantId(deptName, tenantId)) {
            throw new IllegalArgumentException("Department '" + deptName + "' already exists in this tenant workspace");
        }

        Department department = Department.builder()
                .name(deptName)
                .description(request.getDescription() != null ? request.getDescription().trim() : null)
                .status(request.getStatus() != null ? request.getStatus() : Status.ACTIVE)
                .tenantId(tenantId)
                .build();

        Department saved = departmentRepository.save(department);
        log.info("Created department '{}' (id: {}, status: {}) for tenant '{}'",
                saved.getName(), saved.getId(), saved.getStatus(), tenantId);
        return DepartmentResponse.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DepartmentResponse> getAllDepartments(String search, Status status, Pageable pageable) {
        String tenantId = requireTenantContext();

        Map<String, Object> exactFilters = status != null ? Map.of("status", status) : null;

        Specification<Department> spec = GenericSpecification.searchAndFilter(
                tenantId,
                search,
                List.of("name", "description"),
                exactFilters
        );

        return departmentRepository.findAll(spec, pageable).map(DepartmentResponse::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public DepartmentResponse getDepartmentById(Integer id) {
        String tenantId = requireTenantContext();
        Department dept = departmentRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + id));
        return DepartmentResponse.fromEntity(dept);
    }

    @Override
    @Transactional
    public DepartmentResponse updateDepartment(Integer id, UpdateDepartmentRequest request) {
        String tenantId = requireTenantContext();
        Department dept = departmentRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + id));

        String newName = request.getName().trim();
        if (departmentRepository.existsByNameIgnoreCaseAndTenantIdAndIdNot(newName, tenantId, id)) {
            throw new IllegalArgumentException("Department '" + newName + "' already exists in this tenant workspace");
        }

        dept.setName(newName);
        if (request.getDescription() != null) {
            dept.setDescription(request.getDescription().trim());
        }
        if (request.getStatus() != null) {
            dept.setStatus(request.getStatus());
        }

        Department updated = departmentRepository.save(dept);
        log.info("Updated department '{}' (id: {}, status: {}) for tenant '{}'",
                updated.getName(), updated.getId(), updated.getStatus(), tenantId);
        return DepartmentResponse.fromEntity(updated);
    }

    @Override
    @Transactional
    public void deleteDepartment(Integer id) {
        String tenantId = requireTenantContext();
        Department dept = departmentRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + id));

        departmentRepository.delete(dept);
        log.info("Deleted department '{}' (id: {}) from tenant '{}'", dept.getName(), dept.getId(), tenantId);
    }
}
