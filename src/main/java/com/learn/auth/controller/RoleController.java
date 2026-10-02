package com.learn.auth.controller;

import com.learn.auth.dtos.ApiResponse;
import com.learn.auth.dtos.CreateRoleRequest;
import com.learn.auth.dtos.RoleResponse;
import com.learn.auth.dtos.RoleStatsResponse;
import com.learn.auth.dtos.UpdateRolePermissionsRequest;
import com.learn.auth.dtos.UpdateRoleRequest;
import com.learn.auth.entities.Permission;
import com.learn.auth.entities.Status;
import com.learn.auth.service.RoleService;
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
import java.util.Set;

@RestController
@RequestMapping("/roles")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @PreAuthorize("hasAuthority('role.view')")
    @GetMapping({"/stats"})
    public ResponseEntity<ApiResponse<RoleStatsResponse>> getRoleStats() {
        RoleStatsResponse stats = roleService.getRoleStats();
        return ResponseEntity.ok(ApiResponse.success("Role stats fetched successfully", stats));
    }

    @PreAuthorize("hasAuthority('role.create')")
    @PostMapping
    public ResponseEntity<ApiResponse<RoleResponse>> createRole(@RequestBody @Valid CreateRoleRequest request) {
        RoleResponse created = roleService.createRole(request);
        return new ResponseEntity<>(ApiResponse.success("Role created successfully", created), HttpStatus.CREATED);
    }

    @PreAuthorize("hasAuthority('role.view')")
    @GetMapping
    public ResponseEntity<ApiResponse<List<RoleResponse>>> getAllRoles(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Status status,
            @RequestParam(required = false) Integer departmentId,
            @PageableDefault(page = 0, size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<RoleResponse> roles = roleService.getAllRoles(search, status, departmentId, pageable);
        return ResponseEntity.ok(ApiResponse.success("Roles fetched successfully", roles));
    }

    @PreAuthorize("hasAuthority('role.view')")
    @GetMapping("/{roleId}")
    public ResponseEntity<ApiResponse<RoleResponse>> getRoleById(@PathVariable Long roleId) {
        RoleResponse role = roleService.getRoleById(roleId);
        return ResponseEntity.ok(ApiResponse.success("Role fetched successfully", role));
    }

    @PreAuthorize("hasAuthority('role.update')")
    @PutMapping("/{roleId}")
    public ResponseEntity<ApiResponse<RoleResponse>> updateRole(
            @PathVariable Long roleId,
            @RequestBody UpdateRoleRequest request) {
        RoleResponse updated = roleService.updateRole(roleId, request);
        return ResponseEntity.ok(ApiResponse.success("Role updated successfully", updated));
    }

    @PreAuthorize("hasAuthority('role.delete')")
    @DeleteMapping("/{roleId}")
    public ResponseEntity<ApiResponse<Void>> deleteRole(@PathVariable Long roleId) {
        roleService.deleteRole(roleId);
        return ResponseEntity.ok(ApiResponse.success("Role deleted successfully"));
    }

    @PreAuthorize("hasAuthority('role.view')")
    @GetMapping("/{roleId}/permissions")
    public ResponseEntity<ApiResponse<Set<Permission>>> getPermissionByRole(@PathVariable Long roleId) {
        Set<Permission> permissions = roleService.getPermissionsByRoleId(roleId);
        return ResponseEntity.ok(ApiResponse.success("Role permissions fetched successfully", permissions));
    }

    @PreAuthorize("hasAnyAuthority('role.permission_update', 'role.update')")
    @PutMapping("/{roleId}/permissions")
    public ResponseEntity<ApiResponse<RoleResponse>> updateRolePermissions(
            @PathVariable Long roleId,
            @RequestBody UpdateRolePermissionsRequest request) {
        RoleResponse updated = roleService.updateRolePermissions(roleId, request.getPermissionIds());
        return ResponseEntity.ok(ApiResponse.success("Role permissions updated successfully", updated));
    }
}
