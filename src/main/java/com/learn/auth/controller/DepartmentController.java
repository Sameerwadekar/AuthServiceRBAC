package com.learn.auth.controller;

import com.learn.auth.dtos.*;
import com.learn.auth.entities.Status;
import com.learn.auth.service.DepartmentService;
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
@RequestMapping("/departments")
public class DepartmentController {

    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @PreAuthorize("hasAnyAuthority('department.create', 'ROLE_COMPANY_ADMIN')")
    @PostMapping
    public ResponseEntity<ApiResponse<DepartmentResponse>> createDepartment(
            @RequestBody @Valid CreateDepartmentRequest request) {
        DepartmentResponse response = departmentService.createDepartment(request);
        return new ResponseEntity<>(ApiResponse.success("Department created successfully", response), HttpStatus.CREATED);
    }

    @PreAuthorize("hasAnyAuthority('department.view', 'ROLE_COMPANY_ADMIN')")
    @GetMapping
    public ResponseEntity<ApiResponse<List<DepartmentResponse>>> getAllDepartments(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Status status,
            @PageableDefault(page = 0, size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<DepartmentResponse> departments = departmentService.getAllDepartments(search, status, pageable);
        return ResponseEntity.ok(ApiResponse.success("Departments fetched successfully", departments));
    }

    @PreAuthorize("hasAnyAuthority('department.view', 'ROLE_COMPANY_ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DepartmentResponse>> getDepartmentById(@PathVariable Integer id) {
        DepartmentResponse department = departmentService.getDepartmentById(id);
        return ResponseEntity.ok(ApiResponse.success("Department fetched successfully", department));
    }

    @PreAuthorize("hasAnyAuthority('department.update', 'ROLE_COMPANY_ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<DepartmentResponse>> updateDepartment(
            @PathVariable Integer id,
            @RequestBody @Valid UpdateDepartmentRequest request) {
        DepartmentResponse updated = departmentService.updateDepartment(id, request);
        return ResponseEntity.ok(ApiResponse.success("Department updated successfully", updated));
    }

    @PreAuthorize("hasAnyAuthority('department.delete', 'ROLE_COMPANY_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteDepartment(@PathVariable Integer id) {
        departmentService.deleteDepartment(id);
        return ResponseEntity.ok(ApiResponse.success("Department deleted successfully"));
    }
}
